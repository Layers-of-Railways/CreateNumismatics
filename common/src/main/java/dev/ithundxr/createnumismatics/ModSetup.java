package dev.ithundxr.createnumismatics;

import dev.ithundxr.createnumismatics.registry.*;

public class ModSetup {
    public static void register() {
        NumismaticsCreativeModeTabs.register();
        NumismaticsItems.register();
        NumismaticsBlockEntities.register();
        NumismaticsBlocks.register();
        NumismaticsMenuTypes.register();
        NumismaticsTags.register();
    }
}
