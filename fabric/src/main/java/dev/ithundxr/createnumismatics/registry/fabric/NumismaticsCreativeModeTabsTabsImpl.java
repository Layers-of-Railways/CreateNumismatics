package dev.ithundxr.createnumismatics.registry.fabric;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsCreativeModeTabs;

public class NumismaticsCreativeModeTabsTabsImpl {
    public static void use(NumismaticsCreativeModeTabs.Tabs tab) {
        Numismatics.registrate().useCreativeTab(tab.getKey());
    }
}
