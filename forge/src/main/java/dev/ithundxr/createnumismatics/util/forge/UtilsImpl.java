package dev.ithundxr.createnumismatics.util.forge;

import net.minecraftforge.fml.loading.FMLLoader;

public class UtilsImpl {
    public static String platformName() {
        return "Forge";
    }

    @SuppressWarnings("UnstableApiUsage")
    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }
}
