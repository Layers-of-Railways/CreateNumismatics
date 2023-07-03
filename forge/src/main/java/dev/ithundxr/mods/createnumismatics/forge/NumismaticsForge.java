package dev.ithundxr.mods.createnumismatics.forge;

import dev.ithundxr.mods.createnumismatics.Numismatics;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Numismatics.MOD_ID)
public class NumismaticsForge {
    public NumismaticsForge() {
        Numismatics.init();
    }
}