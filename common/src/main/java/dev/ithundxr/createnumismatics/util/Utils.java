package dev.ithundxr.createnumismatics.util;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class Utils {
    @ExpectPlatform
    public static boolean isDevEnv() {
        throw new AssertionError();
    }
}
