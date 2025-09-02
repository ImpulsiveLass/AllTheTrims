package java.com.bawnorton.allthetrims.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.*;
import java.com.bawnorton.allthetrims.Compat;
import java.com.bawnorton.allthetrims.client.util.PaletteHelper;
import java.util.List;
import java.util.Optional;

@Debug(export = true)
@Mixin(ArmorTrim.class)
public abstract class ArmorTrimMixin {
    @Shadow
    public static Optional<ArmorTrim> getTrim(DynamicRegistryManager registryManager, ItemStack stack) {
        throw new AssertionError();
    }

    @WrapOperation(method = "appendTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/text/MutableText;append(Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"))
    private static MutableText updateColour(
            MutableText instance,
            Text text,
            Operation<MutableText> original,
            ItemStack stack,
            DynamicRegistryManager registryManager,
            List<Text> tooltip
    ) {
        ArmorTrim trim = getTrim(registryManager, stack).orElseThrow(AssertionError::new);
        Item trimItem = trim.getMaterial().value().ingredient().value();

        // Fetch cached palette
        List<Color> palette = PaletteHelper.getPalette(trimItem);

        // First color for tooltip
        Color tooltipColor = palette.isEmpty() ? Color.WHITE : palette.get(0);

        MutableText originalText = original.call(instance, text);
        return originalText.styled(style -> style.withColor(tooltipColor.getRGB()));
    }

    @WrapWithCondition(method = "appendTooltip",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2))
    private static <E> boolean dontAddMaterialIfBetterTrimTooltipsLoaded(List<E> instance, E e) {
        return !Compat.isBetterTrimTooltipsIsLoaded();
    }
}
