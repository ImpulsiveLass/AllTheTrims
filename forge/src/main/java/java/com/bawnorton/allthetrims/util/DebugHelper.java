package java.com.bawnorton.allthetrims.util;

import dev.architectury.platform.Platform;
import net.minecraft.item.ItemStack;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.com.bawnorton.allthetrims.AllTheTrims;
import java.com.bawnorton.allthetrims.config.Config;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class DebugHelper {
    static {
        try {
            Path gameDir = Platform.getGameFolder();
            File debugDir = gameDir.resolve("att-debug").toFile();
            if (debugDir.exists()) {
                debugDir.delete();
            }
            debugDir.mkdirs();
        } catch (Exception e) {
            AllTheTrims.LOGGER.error("Failed to create debug directory", e);
        }
    }

    public static void createDebugFile(String directory, String filename, String content) {
        if (!Config.getInstance().debug) return;
        try {
            Path gameDir = Platform.getGameFolder();
            File debugDir = gameDir.resolve("att-debug").resolve(directory).toFile();
            debugDir.mkdirs();
            File debugFile = debugDir.toPath().resolve(filename.replace("/", "_").replace(":", "_")).toFile();
            debugFile.createNewFile();

            Writer writer = new FileWriter(debugFile);
            IOUtils.copy(IOUtils.toInputStream(content, "UTF-8"), writer, "UTF-8");
            writer.close();
        } catch (IOException e) {
            AllTheTrims.LOGGER.error("Failed to create debug file: " + filename, e);
        }
    }

    public static void saveLayeredTexture(BufferedImage image, String path) {
        if (!Config.getInstance().debug) return;
        try {
            Path gameDir = Platform.getGameFolder();
            File debugDir = gameDir.resolve("att-debug").resolve("textures").toFile();
            debugDir.mkdirs();
            File debugFile = debugDir.toPath().resolve(path.replace("/", "_").replace(":", "_")).toFile();
            debugFile.createNewFile();

            ImageIO.write(image, "png", debugFile);
        } catch (IOException e) {
            AllTheTrims.LOGGER.error("Failed to create debug image: " + path, e);
        }
    }

    public static void savePalette(ItemStack stack, BufferedImage image, String path) {
        if (!Config.getInstance().debug) return;
        if ("minecraft:coal.png".equals(path) ||
//"minecraft:charcoal.png".equals(path) ||
"minecraft:flint.png".equals(path) ||
"minecraft:glowstone_dust.png".equals(path) ||
"minecraft:prismarine_crystals.png".equals(path) ||
"minecraft:echo_shard.png".equals(path) ||
"spelunkery:rock_salt.png".equals(path) ||
"spelunkery:nephrite_chunk.png".equals(path) ||
"spelunkery:cinnabar.png".equals(path) ||
"aether:ambrosium_shard.png".equals(path) ||
"deep_aether:metal_mixture.png".equals(path) ||
"aether_redux:veridium_ingot.png".equals(path) ||
"galosphere:allurite_shard.png".equals(path) ||
"galosphere:lumiere_shard.png".equals(path) ||
"galosphere:silver_ingot.png".equals(path) ||
"luminous_nether:ectoplasm_ball.png".equals(path) ||
"outer_end:rose_crystal_shard.png".equals(path) ||
"outer_end:mint_crystal_shard.png".equals(path) ||
"outer_end:cobalt_crystal_shard.png".equals(path) ||
"alexscaves:scarlet_neodymium_ingot.png".equals(path) ||
"alexscaves:azure_neodymium_ingot.png".equals(path) ||
"alexscaves:uranium.png".equals(path) ||
"alexscaves:radiant_essence.png".equals(path) ||
"alexscaves:tectonic_shard.png".equals(path) ||
"alexscaves:pearl.png".equals(path) ||
"alexscaves:occult_gem.png".equals(path) ||
//"supplementaries:ash_brick.png".equals(path) ||
"galosphere:pink_salt_shard.png".equals(path) ||
"minecraft:blaze_powder.png".equals(path) ||
"netherexp:ancient_wax.png".equals(path) ||
"aether_redux:refined_sentrite.png".equals(path) ||
"outer_end:floral_paste.png".equals(path) ||
"phantasm:crystal_shard.png".equals(path) ||
"phantasm:void_crystal_shard.png".equals(path) ||
"farmersdelight:canvas.png".equals(path) ||
"quark:red_corundum_cluster.png".equals(path) ||
"quark:orange_corundum_cluster.png".equals(path) ||
"quark:yellow_corundum_cluster.png".equals(path) ||
"quark:green_corundum_cluster.png".equals(path) ||
"quark:blue_corundum_cluster.png".equals(path) ||
"quark:indigo_corundum_cluster.png".equals(path) ||
"quark:violet_corundum_cluster.png".equals(path) ||
"quark:white_corundum_cluster.png".equals(path) ||
"quark:black_corundum_cluster.png".equals(path) ||
"consistency_plus:glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:red_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:orange_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:yellow_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:lime_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:green_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:blue_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:light_blue_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:magenta_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:cyan_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:purple_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:pink_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:white_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:gray_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:light_gray_glazed_terracotta_brick.png".equals(path) ||
"consistency_plus:black_glazed_terracotta_brick.png".equals(path) ||
                "ancient_aether:valkyrum.png".equals(path) ||
                "deep_aether:skyjade.png".equals(path) ||
                "deep_aether:stratus_ingot.png".equals(path) ||
                "enderitemod:enderite_ingot.png".equals(path) ||
                "aether:zanite_gemstone.png".equals(path) ||
                "aether:golden_amber.png".equals(path) ||
                "aether_redux:gravitite_ingot.png".equals(path) ||
                "vanillabackport:resin_brick.png".equals(path) ||
                "consistency_plus:brown_glazed_terracotta_brick.png".equals(path))  {
            try {
                Path gameDir = Platform.getGameFolder();
                File debugDir = gameDir.resolve("att-debug").resolve("palettes").toFile();
                debugDir.mkdirs();
                File debugFile = debugDir.toPath().resolve(path.replace("/", "_").replace(":", "_")).toFile();
                debugFile.createNewFile();

                ImageIO.write(image, "png", debugFile);
            } catch (IOException e) {
                AllTheTrims.LOGGER.error("Failed to create debug image: " + path, e);
            }
        }
    }
}