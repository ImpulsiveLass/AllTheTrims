package com.bawnorton.allthetrims.client.util;

import com.bawnorton.allthetrims.AllTheTrims;
import com.bawnorton.allthetrims.json.TrimMaterialJson;
import com.bawnorton.allthetrims.util.DebugHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.ColorHelper;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public abstract class PaletteHelper {
    public static final int PALETTE_SIZE = 8; // <-- new constant
    public static final List<Color> WHITE_PALETTE;
    private static final Map<Identifier, List<Color>> PALETTES = new HashMap<>();
    private static final List<Color> BLANK_PALETTE;

    static {
        BLANK_PALETTE = new ArrayList<>();
        WHITE_PALETTE = DefaultedList.ofSize(PALETTE_SIZE, new Color(255, 255, 255));
        for (int i = PALETTE_SIZE; i > 0; i--) {
            BLANK_PALETTE.add(new Color(255 - 255 / i, 255 - 255 / i, 255 - 255 / i));
        }
    }

    public static boolean paletteExists(Identifier identifier) {
        return PALETTES.containsKey(identifier);
    }

    public static List<Color> getPalette(Item item) {
        return getPalette(Registries.ITEM.getId(item));
    }

    public static List<Color> getPalette(Identifier identifier) {
        if (PALETTES.containsKey(identifier)) return PALETTES.get(identifier);

        Item item = Registries.ITEM.get(identifier);
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemModels models = itemRenderer.getModels();
        BakedModel model = models.getModel(item);
        if (model == null) {
            AllTheTrims.LOGGER.warn("Item " + item.getName().getString() + " has no model, using blank palette");
            putPalette(identifier, BLANK_PALETTE);
            return BLANK_PALETTE;
        }

        Sprite sprite = model.getParticleSprite();
        if (sprite == null) {
            AllTheTrims.LOGGER.warn("Model of item " + item.getName()
                                                           .getString() + " has no particle sprite, using blank palette");
            putPalette(identifier, BLANK_PALETTE);
            return BLANK_PALETTE;
        }

        SpriteContents content = sprite.getContents();
        if (content.getDistinctFrameCount().count() <= 0) {
            AllTheTrims.LOGGER.warn("Sprite of item " + item.getName()
                                                            .getString() + " has no frames, using blank palette");
            putPalette(identifier, BLANK_PALETTE);
            return BLANK_PALETTE;
        }

        putPalette(identifier, PaletteGenerator.generatePalette(content.image));
        return PALETTES.get(identifier);
    }

    public static void putPalette(Identifier identifier, List<Color> palette) {
        DebugHelper.savePalette(Registries.ITEM.get(identifier).getDefaultStack(), ImageUtil.colourListToPaletteImage(palette), identifier + ".png");
        PALETTES.put(identifier, palette);
    }

    public static List<Color> existingResourceToPalette(Resource resource) {
        try {
            return PaletteGenerator.toPalette(NativeImage.read(resource.getInputStream()));
        } catch (Exception e) {
            AllTheTrims.LOGGER.error("Failed to read palette image", e);
            return BLANK_PALETTE;
        }
    }

    private abstract static class PaletteGenerator {
        public static List<Color> generatePalette(NativeImage image) {
            Map<Color, Integer> colourMap = new HashMap<>();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int pixel = image.getColor(x, y);
                    int a = pixel >> 24 & 0xFF;
                    if (a >= 5) {
                        Color colour = extractColour(pixel, a);
                        colourMap.put(colour, colourMap.getOrDefault(colour, 0) + 1);
                    }
                }
            }

            Set<Color> colourSet = colourMap.keySet();
            List<Color> colours = new ArrayList<>(colourSet);
            if (colours.isEmpty()) return BLANK_PALETTE;

            return coloursToPalette(removeGreys(colours));
        }

        private static List<Color> removeGreys(List<Color> colours) {
            List<Color> greys = new ArrayList<>();
            List<Color> nonGreys = new ArrayList<>();

            for (Color colour : colours) {
                float[] hsb = Color.RGBtoHSB(colour.getRed(), colour.getGreen(), colour.getBlue(), null);
                if (hsb[1] < 0.2) {
                    greys.add(colour);
                } else {
                    nonGreys.add(colour);
                }
            }

            double greyRatio = (double) greys.size() / colours.size();

            if (greyRatio >= 0.33333) {
                nonGreys.addAll(greys);
            }

            if (nonGreys.isEmpty()) {
                return greys;
            }

            return nonGreys;
        }

        private static List<Color> coloursToPalette(List<Color> colours) {
            Map<Color, Integer> counts = new HashMap<>();
            for (Color c : colours) {
                counts.put(c, counts.getOrDefault(c, 0) + 1);
            }

            // Sort by frequency
            List<Color> sorted = new ArrayList<>(counts.keySet());
            sorted.sort((a, b) -> counts.get(b) - counts.get(a));

            List<Color> unique = new ArrayList<>();
            int minDistance = 100; // start tolerance
            while (unique.size() < PALETTE_SIZE && minDistance >= 1) {
                unique.clear(); // redo selection fresh each pass

                for (Color c : sorted) {
                    boolean tooClose = false;
                    for (Color u : unique) {
                        if (colorDistance(c, u) < minDistance) {
                            tooClose = true;
                            break;
                        }
                    }
                    if (!tooClose) {
                        unique.add(c);
                    }
                    if (unique.size() >= PALETTE_SIZE) break;
                }

                minDistance -= 1; // relax filter each pass
            }

            if (unique.size() < PALETTE_SIZE && !unique.isEmpty()) {
                int index = 0;
                int originalCount = unique.size();
                while (unique.size() < PALETTE_SIZE) {
                    Color c = unique.get(index % originalCount);
                    double factor = 0.8 - 0.05 * (index + 1);
                    int r = (int)(c.getRed() * factor);
                    int g = (int)(c.getGreen() * factor);
                    int b = (int)(c.getBlue() * factor);
                    unique.add(new Color(
                            Math.max(0, r),
                            Math.max(0, g),
                            Math.max(0, b)
                    ));
                    index++;
                }
            }

            // --- Nearest-neighbor ordering ---
            if (!unique.isEmpty()) {
                List<Color> ordered = new ArrayList<>();
                Set<Color> remaining = new HashSet<>(unique);

                double minIntialDistance = remaining.stream()
                        .mapToDouble(c -> colorDistanceHSB(c, Color.white))
                        .min()
                        .orElse(Double.MAX_VALUE);

                List<Color> minIntialColors = remaining.stream()
                        .filter(c -> colorDistanceHSB(c, Color.white) == minIntialDistance)
                        .toList();

                Color current = minIntialColors.stream()
                        .max(Comparator.comparingDouble(c -> {
                            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                            return hsb[1]; // saturation is at index 1
                        }))
                        .orElse(null);
                        /*.max(Comparator.comparingDouble(c -> {
                            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);

                            float hue = hsb[0];         // 0.0 - 1.0
                            float sat = hsb[1];         // 0.0 - 1.0
                            float bri = hsb[2];         // 0.0 - 1.0

                            // Weight hue heavily (e.g. 100x), then add brightness as a smaller factor
                            // so neon gets chosen before pastel if hues are close
                            return Math.sqrt(hue * hue * 1 + bri * bri * 1.2);
                        }))
                        .orElse(unique.get(0));*/

                ordered.add(current);
                remaining.remove(current);

                while (!remaining.isEmpty()) {
                    final Color cur = current;

                    double minRemainingDistance = remaining.stream()
                            .mapToDouble(c -> colorDistanceHSB(c, cur))
                            .min()
                            .orElse(Double.MAX_VALUE);

                    List<Color> minRemainingColors = remaining.stream()
                            .filter(c -> colorDistanceHSB(c, cur) == minRemainingDistance)
                            .toList();

                    Color next = minRemainingColors.stream()
                            .max(Comparator.comparingDouble(c -> {
                                float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                                return hsb[1]; // saturation is at index 1
                            }))
                            .orElse(null);

                    /*Color next = remaining.stream()
                            .min(Comparator.comparingDouble(c -> {
                                float[] hsbCur = Color.RGBtoHSB(cur.getRed(), cur.getGreen(), cur.getBlue(), null);
                                float[] hsbNext = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);

                                double dh = Math.min(
                                        Math.abs(hsbCur[0] - hsbNext[0]),
                                        1.0 - Math.abs(hsbCur[0] - hsbNext[0])
                                );

                                //double ds = Math.abs(hsbCur[1] - hsbNext[1]);
                                double db = Math.abs(hsbCur[2] - hsbNext[2]);

                                return dh * 1 + db * 0.5;
                            }))
                            .get();*/

                    ordered.add(next);
                    remaining.remove(next);
                    current = next;
                }

                unique = ordered;
            }

            if (unique.size() > PALETTE_SIZE) {
                return unique.subList(0, PALETTE_SIZE);
            } else {
                return unique;
            }
        }

        private static double colorDistance(Color c1, Color c2) {
            int dr = c1.getRed() - c2.getRed();
            int dg = c1.getGreen() - c2.getGreen();
            int db = c1.getBlue() - c2.getBlue();
            return Math.sqrt(dr * dr + dg * dg + db * db);
        }

        private static double colorDistanceHSB(Color c1, Color c2)
        {
            float[] hsb1 = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null);
            float[] hsb2 = Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), null);

            if (hsb2[1] <= 0)
                hsb2[0] = 0;

            double dh = Math.min(
                    Math.abs(hsb1[0] - hsb2[0]),
                    1.0 - Math.abs(hsb1[0] - hsb2[0])
            );

            //hsb2[2] += 0.159174f;
            double ds = Math.abs(hsb1[1] - hsb2[1]);

            double db = Math.abs(hsb1[2] - hsb2[2]);

            return Math.sqrt(dh * dh + ds * ds //* 0.125
                    + db * db //2.34527
                     );
        }

        private static double weightedColorDistance(Color c1, Color c2) {
            double dr = c1.getRed() - c2.getRed();
            double dg = c1.getGreen() - c2.getGreen();
            double db = c1.getBlue() - c2.getBlue();

            // Human perception weights (approx Rec. 709 / sRGB luminance factors)
            double wr = 1f/0.2126;
            double wg = 1f/0.7152;
            double wb = 1f/0.0722;

            return Math.sqrt(wr * dr * dr + 16 * wg * dg * dg + 256 * wb * db * db);
        }

        private static Color weightColors(Color color) {
            double rr = color.getRed() * (1/0.2126);
            double rg = color.getGreen() * (1/0.7152);
            double rb = color.getBlue() * (1/0.0722);

            return new Color((int)rr, (int)rg, (int)rb);
        }

        public static double deltaE(Color c1, Color c2) {
            double[] lab1 = rgbToLab(c1.getRed(), c1.getGreen(), c1.getBlue());
            double[] lab2 = rgbToLab(c2.getRed(), c2.getGreen(), c2.getBlue());

            double dL = lab1[0] - lab2[0];
            double da = lab1[1] - lab2[1];
            double db = lab1[2] - lab2[2];

            return Math.sqrt(dL * dL + 4 * da * da + db * db);
        }

        // Convert RGB -> LAB
        private static double[] rgbToLab(int r, int g, int b) {
            // 1) sRGB to XYZ
            double[] xyz = rgbToXyz(r, g, b);

            // 2) XYZ to Lab
            return xyzToLab(xyz[0], xyz[1], xyz[2]);
        }

        private static double[] rgbToXyz(int r, int g, int b) {
            // normalize to 0-1
            double rNorm = pivotRgb(r / 255.0);
            double gNorm = pivotRgb(g / 255.0);
            double bNorm = pivotRgb(b / 255.0);

            // linear transform (D65 illuminant)
            double x = rNorm * 0.4124564 + gNorm * 0.3575761 + bNorm * 0.1804375;
            double y = rNorm * 0.2126729 + gNorm * 0.7151522 + bNorm * 0.0721750;
            double z = rNorm * 0.0193339 + gNorm * 0.1191920 + bNorm * 0.9503041;

            return new double[]{x, y, z};
        }

        private static double pivotRgb(double c) {
            return (c <= 0.04045) ? (c / 12.92) : Math.pow((c + 0.055) / 1.055, 2.4);
        }

        private static double[] xyzToLab(double x, double y, double z) {
            // reference white (D65)
            double xr = x / 0.95047;
            double yr = y / 1.00000;
            double zr = z / 1.08883;

            double fx = pivotXyz(xr);
            double fy = pivotXyz(yr);
            double fz = pivotXyz(zr);

            double L = Math.max(0, 116 * fy - 16);
            double a = 500 * (fx - fy);
            double b = 200 * (fy - fz);

            return new double[]{L, a, b};
        }

        private static double pivotXyz(double t) {
            return (t > Math.pow(6.0 / 29.0, 3))
                    ? Math.cbrt(t)
                    : (t / (3 * Math.pow(6.0 / 29.0, 2)) + 4.0 / 29.0);
        }

        private static List<Color> stretchColors(Color... originalColors) {
            List<Color> stretchedColors = new ArrayList<>();

            int segmentCount = 7;
            double segmentSize = (double) segmentCount / (originalColors.length - 1);
            for (int i = 0; i < originalColors.length - 1; i++) {
                Color startColor = originalColors[i];
                Color endColor = originalColors[i + 1];
                for (int j = 0; j < segmentSize; j++) {
                    float ratio = (float) j / (float) segmentSize;
                    int red = (int) (startColor.getRed() + ratio * (endColor.getRed() - startColor.getRed()));
                    int green = (int) (startColor.getGreen() + ratio * (endColor.getGreen() - startColor.getGreen()));
                    int blue = (int) (startColor.getBlue() + ratio * (endColor.getBlue() - startColor.getBlue()));
                    stretchedColors.add(new Color(red, green, blue));
                }
            }

            stretchedColors.add(originalColors[originalColors.length - 1]);
            return stretchedColors;
        }

        public static List<Color> toPalette(NativeImage image) {
            if (image.getWidth() != PALETTE_SIZE || image.getHeight() != 1) {
                //AllTheTrims.LOGGER.warn("Palette image is not 16x1 pixels, using blank palette" + image.getWidth());
                return BLANK_PALETTE;
            }

            List<Color> palette = new ArrayList<>();
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getColor(x, 0);
                int a = pixel >> 24 & 0xFF;
                if (a >= 5) {
                    Color colour = extractColour(pixel, a);
                    palette.add(colour);
                }
            }
            return palette;
        }

        private static Color extractColour(int pixel, int alpha) {
            int[] argb = new int[4];
            argb[0] = alpha;
            argb[1] = (pixel & 0xFF);
            argb[2] = (pixel >> 8 & 0xFF);
            argb[3] = (pixel >> 16 & 0xFF);
            return new Color(ColorHelper.Argb.getArgb(argb[0], argb[1], argb[2], argb[3]), true);
        }
    }
    /*public static void addTrimJson(Map<Identifier, Resource> original, Map.Entry<Identifier, Resource> first, Identifier itemId, Item item, Color mainColor) {
        String hexColor = String.format("#%06X", mainColor.getRGB() & 0xFFFFFF);

        TrimMaterialJson trimMaterialJson = new TrimMaterialJson(
                AllTheTrims.TRIM_ASSET_NAME,
                hexColor,
                Text.translatable("text.allthetrims.material", item.getName().getString()).getString(),
                itemId.toString(),
                Float.MAX_VALUE
        );

        JsonObject resourceJson = trimMaterialJson.asJson();
        Resource resource = new Resource(
                first.getValue().getPack(),
                () -> IOUtils.toInputStream(resourceJson.toString(), "UTF-8")
        );
        Identifier resourceId = new Identifier(itemId.getNamespace(), "trim_material/" + itemId.getPath() + ".json");
        original.put(resourceId, resource);

        DebugHelper.createDebugFile("trim_materials", itemId + ".json", resourceJson.toString());
    }*/

    public static TextColor getTooltipColor(Item item) {
        List<Color> palette = PaletteHelper.getPalette(item);
        if (palette.isEmpty()) return TextColor.fromRgb(0xFFFFFF); // fallback white

        Color color = palette.get(0); // pick first color
        return TextColor.fromRgb(color.getRGB());
    }

    public static void putJsonPalette(Identifier ingredientId, TrimMaterialJson trimMaterial) {
        if (trimMaterial == null || trimMaterial.description() == null || trimMaterial.description().colors() == null) {
            AllTheTrims.LOGGER.warn("Invalid TrimMaterialJson for {}", ingredientId);
            PALETTES.put(ingredientId, new ArrayList<>(WHITE_PALETTE));
            return;
        }

        List<Color> palette = new ArrayList<>();
        for (String hex : trimMaterial.description().colors()) {
            try {
                palette.add(Color.decode(hex));
            } catch (NumberFormatException e) {
                AllTheTrims.LOGGER.warn("Invalid color {} in {}. Using WHITE.", hex, ingredientId);
                palette.add(Color.WHITE);
            }
        }

        if (palette.isEmpty()) {
            palette.addAll(WHITE_PALETTE);
        }

        PALETTES.put(ingredientId, palette);
        AllTheTrims.LOGGER.debug("Palette loaded from JSON for {}: {}", ingredientId, palette);
    }
    private static final Map<String, TrimMaterialJson> PALETTE_CACHE = new HashMap<>();

    public static void putJsonPalette(String itemId, TrimMaterialJson json) {
        PALETTE_CACHE.put(itemId, json);
    }

    public static List<Color> getPalette(String itemId) {
        TrimMaterialJson json = PALETTE_CACHE.get(itemId);
        if (json == null) return List.of(Color.WHITE);

        return json.description().colors().stream()
                .map(hex -> {
                    try {
                        return Color.decode(hex);
                    } catch (NumberFormatException e) {
                        return Color.WHITE;
                    }
                })
                .toList();
    }

    public static void cacheFromNbt(ItemStack stack, NbtCompound trimTag) {
        String materialId = trimTag.getString("material");
        TrimMaterialJson json = PALETTE_CACHE.get(materialId);
        if (json != null) {
            putJsonPalette(stack.getItem().toString(), json);
        }
    }
}
