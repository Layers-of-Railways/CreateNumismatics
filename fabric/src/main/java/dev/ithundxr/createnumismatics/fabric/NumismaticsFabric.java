package dev.ithundxr.createnumismatics.fabric;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import dev.ithundxr.createnumismatics.ExampleBlocks;
import dev.ithundxr.createnumismatics.Numismatics;
import net.fabricmc.api.ModInitializer;

public class NumismaticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Numismatics.init();
        Numismatics.LOGGER.info(EnvExecutor.unsafeRunForDist(
                () -> () -> "{} is accessing Porting Lib on a Fabric client!",
                () -> () -> "{} is accessing Porting Lib on a Fabric server!"
                ), Numismatics.NAME);
        // on fabric, Registrates must be explicitly finalized and registered.
        ExampleBlocks.REGISTRATE.register();
    }
}
