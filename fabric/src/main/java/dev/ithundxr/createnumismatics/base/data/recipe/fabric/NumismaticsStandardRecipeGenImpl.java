package dev.ithundxr.createnumismatics.base.data.recipe.fabric;

import dev.ithundxr.createnumismatics.base.data.recipe.NumismaticsStandardRecipeGen;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

public class NumismaticsStandardRecipeGenImpl extends NumismaticsStandardRecipeGen {
	protected NumismaticsStandardRecipeGenImpl(DataGenerator pGenerator) {
		super(pGenerator);
	}

	public static RecipeProvider create(DataGenerator gen) {
		NumismaticsStandardRecipeGen provider = new NumismaticsStandardRecipeGenImpl(gen);
		return new FabricRecipeProvider((FabricDataGenerator) gen) {
			@Override
			protected void generateRecipes(Consumer<FinishedRecipe> exporter) {
				provider.registerRecipes(exporter);
			}
		};
	}
}
