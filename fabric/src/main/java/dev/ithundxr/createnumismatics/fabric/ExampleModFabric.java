package dev.ithundxr.createnumismatics.fabric;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import dev.ithundxr.createnumismatics.ExampleBlocks;
import dev.ithundxr.createnumismatics.ExampleMod;
import net.fabricmc.api.ModInitializer;

public class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExampleMod.init();
        ExampleMod.LOGGER.info(EnvExecutor.unsafeRunForDist(
                () -> () -> "{} is accessing Porting Lib on a Fabric client!",
                () -> () -> "{} is accessing Porting Lib on a Fabric server!"
                ), ExampleMod.NAME);
        // on fabric, Registrates must be explicitly finalized and registered.
        ExampleBlocks.REGISTRATE.register();
    }
}
