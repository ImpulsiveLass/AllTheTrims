package java.com.bawnorton.allthetrims.client.implementation.emi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.util.Identifier;

import java.com.bawnorton.allthetrims.AllTheTrims;
import java.com.bawnorton.allthetrims.mixin.accessor.SmithingTrimRecipeAccessor;
import java.com.bawnorton.allthetrims.mixin.client.invoker.emi.VanillaPluginInvoker;

public class EmiPluginImpl implements EmiPlugin {
    static final EmiRecipeCategory TRIMMING;

    static {
        TRIMMING = new EmiRecipeCategory(new Identifier(AllTheTrims.MOD_ID, "smithing"), EmiStack.of(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE), VanillaPluginInvoker.invokeSimplifiedRenderer(240, 224), EmiRecipeSorting.compareInputThenOutput());
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(TRIMMING);
        registry.addWorkstation(TRIMMING, EmiStack.of(Items.SMITHING_TABLE));

        for (SmithingRecipe recipe : registry.getRecipeManager().listAllOfType(RecipeType.SMITHING)) {
            if (recipe instanceof SmithingTransformRecipe) continue;
            if (recipe instanceof SmithingTrimRecipeAccessor accessor) {
                registry.addRecipe(new AllTheTrimsSmithingRecipe(EmiIngredient.of(accessor.getTemplate()), EmiIngredient.of(accessor.getBase()), EmiIngredient.of(accessor.getAddition()), EmiStack.of(EmiPort.getOutput(recipe)), recipe));
            }
        }
    }
}
