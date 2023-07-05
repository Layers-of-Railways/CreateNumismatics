package dev.ithundxr.createnumismatics.util;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class Utils {
    @ExpectPlatform
    public static String platformName() {
        // Just throw an error, the content should get replaced at runtime.
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isDevEnv() {
        throw new AssertionError();
    }
}
