package dev.ithundxr.createnumismatics.registry.forge;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsCreativeModeTabs;

import static dev.ithundxr.createnumismatics.registry.forge.NumismaticsCreativeModeTabsRegistrateDisplayItemsGeneratorImpl.getTabObject;

public class NumismaticsCreativeModeTabsTabsImpl {
    public static void use(NumismaticsCreativeModeTabs.Tabs tab) {
        Numismatics.registrate().setCreativeTab(getTabObject(tab.getKey()));
    }
}
