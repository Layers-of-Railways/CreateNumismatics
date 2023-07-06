package dev.ithundxr.createnumismatics;

import dev.ithundxr.createnumismatics.registry.*;

public class ModSetup {
    public static void register() {
        NumismaticsItems.init();
        NumismaticsBlocks.init();
        NumismaticsTags.register();
    }
}
