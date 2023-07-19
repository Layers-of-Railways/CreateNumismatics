package dev.ithundxr.createnumismatics.fabric;

import dev.ithundxr.createnumismatics.Numismatics;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

//fixme
public class NumismaticsDataFabric implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        Path numismaticsResources = Paths.get(System.getProperty(ExistingFileHelper.EXISTING_RESOURCES));
        ExistingFileHelper helper = new ExistingFileHelper(
            Set.of(numismaticsResources), Set.of("create"), false, null, null
        );
        Numismatics.registrate().setupDatagen(gen, helper);
        Numismatics.gatherData(gen);
    }
}
