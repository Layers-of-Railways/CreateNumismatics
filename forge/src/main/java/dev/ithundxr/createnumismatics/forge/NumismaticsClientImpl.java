package dev.ithundxr.createnumismatics.forge;

import dev.ithundxr.createnumismatics.NumismaticsClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class NumismaticsClientImpl {
    public static void init() {
        NumismaticsClient.init();
    }
}
