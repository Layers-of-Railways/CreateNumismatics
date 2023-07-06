package dev.ithundxr.createnumismatics.util.forge;

import net.minecraftforge.fml.loading.FMLLoader;

public class UtilsImpl {
    @SuppressWarnings("UnstableApiUsage")
    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }
}
