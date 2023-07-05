package dev.ithundxr.createnumismatics.forge;

import dev.ithundxr.createnumismatics.ExampleBlocks;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Numismatics.MOD_ID)
public class NumismaticsForge {
    public NumismaticsForge() {
        // registrate must be given the mod event bus on forge before registration
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ExampleBlocks.REGISTRATE.registerEventListeners(eventBus);
        Numismatics.init();
    }
}
