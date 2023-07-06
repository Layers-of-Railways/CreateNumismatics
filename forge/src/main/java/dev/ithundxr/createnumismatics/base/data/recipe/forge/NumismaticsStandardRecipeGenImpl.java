package dev.ithundxr.createnumismatics.base.data.recipe.forge;

import dev.ithundxr.createnumismatics.base.data.recipe.NumismaticsStandardRecipeGen;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class NumismaticsStandardRecipeGenImpl extends NumismaticsStandardRecipeGen {
	protected NumismaticsStandardRecipeGenImpl(DataGenerator pGenerator) {
		super(pGenerator);
	}

	public static RecipeProvider create(DataGenerator gen) {
		NumismaticsStandardRecipeGen provider = new NumismaticsStandardRecipeGenImpl(gen);
		return new RecipeProvider(gen) {
			@Override
			protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
				provider.registerRecipes(consumer);
			}
		};
	}
}
