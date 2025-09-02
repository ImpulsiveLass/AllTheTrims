package java.com.bawnorton.allthetrims.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.texture.NativeImage;

import java.util.ArrayList;
import java.util.List;

public record TrimMaterialJson(String assetName, Description description, String ingredient,
                               Float itemModelIndex) implements JsonRepresentable {

    public TrimMaterialJson(String assetName, List<String> colors, String translate, String ingredient, Float itemModelIndex) {
        this(assetName, new Description(colors, translate), ingredient, itemModelIndex);
    }

    public static TrimMaterialJson fromJson(JsonObject json) {
        return new TrimMaterialJson(
                JsonHelper.getString(json, "asset_name"),
                Description.fromJson(JsonHelper.getObject(json, "description")),
                JsonHelper.getString(json, "ingredient"),
                JsonHelper.getFloat(json, "item_model_index")
        );
    }

    public JsonObject asJson() {
        JsonObject json = new JsonObject();
        json.addProperty("asset_name", assetName);
        json.add("description", description.asJson());
        json.addProperty("ingredient", ingredient);
        json.addProperty("item_model_index", itemModelIndex);
        return json;
    }

    public record Description(List<String> colors, String translate) implements JsonRepresentable {
        public static Description fromJson(JsonObject json) {
            List<String> colors = new ArrayList<>();
            if (json.has("colors")) {
                for (var elem : json.getAsJsonArray("colors")) {
                    colors.add(elem.getAsString());
                }
            } else {
                colors.add("#FFFFFF");
            }
            String translate = JsonHelper.getStringOrElse(json, "translate", "");
            return new Description(colors, translate);
        }

        public JsonObject asJson() {
            JsonObject json = new JsonObject();
            JsonArray array = new JsonArray();
            for (String color : colors) array.add(color);
            json.add("colors", array);
            json.addProperty("translate", translate);
            return json;
        }
    }

    public static List<String> sampleColors(NativeImage image, int samples, List<int[]> coords) {
        List<String> result = new ArrayList<>();
        if (image == null || coords == null || coords.isEmpty()) {
            result.add("#FFFFFF");
            return result;
        }

        for (int i = 0; i < samples; i++) {
            int[] coord = coords.get(i % coords.size());
            int x = coord[0];
            int y = coord[1];

            // clamp to image bounds
            x = Math.min(Math.max(0, x), image.getWidth() - 1);
            y = Math.min(Math.max(0, y), image.getHeight() - 1);

            int pixel = image.getColor(x, y);
            int alpha = (pixel >> 24) & 0xFF;
            if (alpha == 0) continue;
            int b = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int r = pixel & 0xFF;
            String hex = String.format("#%02X%02X%02X", r, g, b);
            if (!result.contains(hex)) {
                result.add(hex);
            }
        }

        if (result.isEmpty()) result.add("#FFFFFF");
        return result;
    }
}
