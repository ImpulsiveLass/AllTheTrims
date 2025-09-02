package java.com.bawnorton.allthetrims.client.implementation.jei;

/*public class JeiPluginImpl implements IModPlugin {
    static final RecipeType<SmithingRecipe> smithingRecipeType = RecipeType.create(AllTheTrims.MOD_ID, "smithing", SmithingRecipe.class);
    static IRecipeCategory<SmithingRecipe> smithingCategory;

    public static boolean isTrimmingRecipe(SmithingRecipe smithingRecipe) {
        return smithingRecipe instanceof SmithingTrimRecipe;
    }

    @Override
    public @NotNull Identifier getPluginUid() {
        return new Identifier(AllTheTrims.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        //registration.addRecipeCategories(smithingCategory = new AllTheTrimsSmithingRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IIngredientManager ingredientManager = registration.getIngredientManager();
        VanillaRecipes vanillaRecipes = new VanillaRecipes(ingredientManager);
        registration.addRecipes(smithingRecipeType, vanillaRecipes.getSmithingRecipes(smithingCategory));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), smithingRecipeType);
    }
}*/
