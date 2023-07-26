package dev.ithundxr.createnumismatics.fabric;

import dev.ithundxr.createnumismatics.Numismatics;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class NumismaticsDataFabric implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        Path numismaticsResources = Paths.get(System.getProperty(ExistingFileHelper.EXISTING_RESOURCES));
        ExistingFileHelper helper = new ExistingFileHelper(
            Set.of(numismaticsResources), Set.of("create"), false, null, null
        );
        FabricDataGenerator.Pack pack = gen.createPack();
        Numismatics.registrate().setupDatagen(pack, helper);
        Numismatics.gatherData(pack);
    }
}
