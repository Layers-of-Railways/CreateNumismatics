package dev.ithundxr.createnumismatics.neoforge;

import com.simibubi.create.Create;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.NumismaticsData;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class NumismaticsDataNeoForge {
	public static void gatherDataHighPriority(GatherDataEvent event) {
		if (event.getMods().contains(Numismatics.MOD_ID))
			NumismaticsData.addRegistrateDataGenerators();
	}

	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();

		CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
		
		NumismaticsData.PROVIDERS.forEach(func -> {
			DataProvider provider = func.apply(output, lookupProvider);
			generator.addProvider(event.includeServer(), provider);
		});
	}
}
