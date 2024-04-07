package dev.ithundxr.createnumismatics;

import dev.ithundxr.createnumismatics.registry.NumismaticsAdvancements;
import dev.ithundxr.createnumismatics.registry.NumismaticsTriggers;

public class ModSetupLate {
    public static void registerPostRegistration() {
        NumismaticsAdvancements.register();
        NumismaticsTriggers.register();
    }
}
