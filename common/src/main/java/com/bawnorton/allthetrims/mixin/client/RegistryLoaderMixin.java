package com.bawnorton.allthetrims.mixin.client;

import com.bawnorton.allthetrims.AllTheTrims;
import com.bawnorton.allthetrims.client.util.PaletteHelper;
import com.bawnorton.allthetrims.json.JsonHelper;
import com.bawnorton.allthetrims.json.TrimMaterialJson;
import com.bawnorton.allthetrims.util.DebugHelper;
import com.bawnorton.allthetrims.util.TrimMaterialHelper;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.resource.Resource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(RegistryLoader.class)
public abstract class RegistryLoaderMixin {

    @ModifyExpressionValue(
            method = "load(Lnet/minecraft/registry/RegistryOps$RegistryInfoGetter;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/MutableRegistry;Lcom/mojang/serialization/Decoder;Ljava/util/Map;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/resource/ResourceFinder;findResources(Lnet/minecraft/resource/ResourceManager;)Ljava/util/Map;")
    )
    private static Map<Identifier, Resource> addAllTrimMaterialJsonFiles(Map<Identifier, Resource> original) {
        Iterator<Map.Entry<Identifier, Resource>> iterator = original.entrySet().iterator();
        if (!iterator.hasNext()) return original;

        Map.Entry<Identifier, Resource> first = iterator.next();
        if (!first.getKey().getPath().contains("trim_material")) return original;

        // load builtin trim_materials first
        for (Map.Entry<Identifier, Resource> resourceEntry : original.entrySet()) {
            try (BufferedReader reader = resourceEntry.getValue().getReader()) {
                JsonObject trimJson = JsonHelper.fromJsonReader(reader, JsonObject.class);
                TrimMaterialHelper.BUILTIN_TRIM_MATERIALS.add(TrimMaterialJson.fromJson(trimJson));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // create dynamic trims for items
        TrimMaterialHelper.forEachTrimMaterial((item, builtin) -> {
            if (builtin) return;

            Identifier itemId = Registries.ITEM.getId(item);
            List<Color> palette = PaletteHelper.getPalette(itemId);

            // If palette is empty, fallback to white
            if (palette.isEmpty()) {
                palette = List.of(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE,
                        Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
            } else if (palette.size() < 8) {
                // Repeat last color until we have 8
                Color last = palette.get(palette.size() - 1);
                while (palette.size() < 8) palette.add(last);
            } else if (palette.size() > 8) {
                // Optional: trim to 8 if more
                palette = palette.subList(0, 8);
            }

            // Convert to hex strings for JSON
            List<String> hexColors = palette.stream()
                    .map(c -> String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()))
                    .toList();

            TrimMaterialJson trimMaterialJson = new TrimMaterialJson(
                    AllTheTrims.TRIM_ASSET_NAME,
                    hexColors,
                    Text.translatable("text.allthetrims.material", item.getName().getString()).getString(),
                    itemId.toString(),
                    Float.MAX_VALUE
            );

            PaletteHelper.putJsonPalette(itemId, trimMaterialJson);

            JsonObject resourceJson = trimMaterialJson.asJson();
            Resource resource = new Resource(first.getValue().getPack(),
                    () -> IOUtils.toInputStream(resourceJson.toString(), "UTF-8"));
            Identifier resourceId = new Identifier(itemId.getNamespace(), "trim_material/" + itemId.getPath() + ".json");
            original.put(resourceId, resource);

            DebugHelper.createDebugFile("trim_materials", itemId + ".json", resourceJson.toString());
        });

        return original;
    }

    @Unique
    private static String allTheTrims$escape(String string) {
        return string.replace("\"", "\\\"");
    }
}
