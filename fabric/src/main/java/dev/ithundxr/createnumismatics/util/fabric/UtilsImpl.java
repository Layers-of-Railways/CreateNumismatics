package dev.ithundxr.createnumismatics.util.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class UtilsImpl {
    public static String platformName() {
        return FabricLoader.getInstance().isModLoaded("quilt_loader") ? "Quilt" : "Fabric";
    }

    public static boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
