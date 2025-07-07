package dev.ithundxr.createnumismatics;

import com.tterrag.registrate.providers.ProviderType;
import dev.ithundxr.createnumismatics.base.data.NumismaticsTagGen;
import dev.ithundxr.createnumismatics.base.data.lang.NumismaticsLangGen;
import dev.ithundxr.createnumismatics.base.data.recipe.NumismaticsStandardRecipeGen;
import dev.ithundxr.createnumismatics.registry.NumismaticsAdvancements;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class NumismaticsData {
	public static final List<BiFunction<PackOutput, CompletableFuture<Provider>, ? extends DataProvider>> PROVIDERS = List.of(
		NumismaticsStandardRecipeGen::new,
		NumismaticsAdvancements::new
	);
	
	public static void addRegistrateDataGenerators() {
		Numismatics.registrate().addDataGenerator(ProviderType.BLOCK_TAGS, NumismaticsTagGen::generateBlockTags);
		Numismatics.registrate().addDataGenerator(ProviderType.ITEM_TAGS, NumismaticsTagGen::generateItemTags);
		Numismatics.registrate().addDataGenerator(ProviderType.LANG, NumismaticsLangGen::generate);
	}
}
