package dev.ithundxr.createnumismatics.util.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class PlatformUtilImpl {
    public static String platformName() {
        return FabricLoader.getInstance().isModLoaded("quilt_loader") ? "Quilt" : "Fabric";
    }
}
