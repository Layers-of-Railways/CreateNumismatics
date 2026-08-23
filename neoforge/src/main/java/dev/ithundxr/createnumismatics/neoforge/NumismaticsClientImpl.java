package dev.ithundxr.createnumismatics.neoforge;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.NumismaticsClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Numismatics.MOD_ID, dist = Dist.CLIENT)
public class NumismaticsClientImpl {
    public NumismaticsClientImpl() {
        NumismaticsClient.init();
    }
}
