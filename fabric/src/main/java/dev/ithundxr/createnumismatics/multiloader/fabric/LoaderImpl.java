package dev.ithundxr.createnumismatics.multiloader.fabric;

import dev.ithundxr.createnumismatics.multiloader.Loader;
import net.fabricmc.loader.api.FabricLoader;

public class LoaderImpl {
    public static Loader getCurrent() {
        return Loader.FABRIC;
    }
}
