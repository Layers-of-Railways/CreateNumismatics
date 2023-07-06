package dev.ithundxr.createnumismatics.util.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class UtilsImpl {
    public static boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
