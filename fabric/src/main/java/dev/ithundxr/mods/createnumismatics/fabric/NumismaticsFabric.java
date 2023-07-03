package dev.ithundxr.mods.createnumismatics.fabric;

import dev.ithundxr.mods.createnumismatics.Numismatics;
import net.fabricmc.api.ModInitializer;

public class NumismaticsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Numismatics.init();
    }
}