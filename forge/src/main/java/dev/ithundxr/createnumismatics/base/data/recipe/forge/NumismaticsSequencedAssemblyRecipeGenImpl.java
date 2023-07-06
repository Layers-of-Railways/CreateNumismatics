package dev.ithundxr.createnumismatics.base.data.recipe.forge;

import dev.ithundxr.createnumismatics.base.data.recipe.NumismaticsSequencedAssemblyRecipeGen;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class NumismaticsSequencedAssemblyRecipeGenImpl extends NumismaticsSequencedAssemblyRecipeGen {
	protected NumismaticsSequencedAssemblyRecipeGenImpl(DataGenerator pGenerator) {
		super(pGenerator);
	}

	public static RecipeProvider create(DataGenerator gen) {
		NumismaticsSequencedAssemblyRecipeGen provider = new NumismaticsSequencedAssemblyRecipeGenImpl(gen);
		return new RecipeProvider(gen) {
			@Override
			protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
				provider.registerRecipes(consumer);
			}
		};
	}
}
