package dev.ithundxr.createnumismatics.registry.neoforge;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsCreativeModeTabs;

public class NumismaticsCreativeModeTabsTabsImpl {
    public static void use(NumismaticsCreativeModeTabs.Tabs tab) {
        Numismatics.registrate().setCreativeTab(NumismaticsCreativeModeTabsImpl.MAIN_TAB);
    }
}
