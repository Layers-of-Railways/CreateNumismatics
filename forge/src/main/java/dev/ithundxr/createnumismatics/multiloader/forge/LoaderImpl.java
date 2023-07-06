package dev.ithundxr.createnumismatics.multiloader.forge;

import dev.ithundxr.createnumismatics.multiloader.Loader;

public class LoaderImpl {
    public static Loader getCurrent() {
        return Loader.FORGE;
    }
}
