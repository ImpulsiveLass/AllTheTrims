package java.com.bawnorton.allthetrims.mixin.client.compat.jei;

/*@Pseudo
@Mixin(SmithingRecipeCategory.class)
public abstract class SmithingRecipeCategoryMixin {
    @ModifyReturnValue(method = "isHandled(Lnet/minecraft/recipe/SmithingRecipe;)Z", at = @At("RETURN"))
    private boolean onlyHandleTransformations(boolean original, SmithingRecipe recipe) {
        return !JeiPluginImpl.isTrimmingRecipe(recipe);
    }
}*/
