package dev.ithundxr.createnumismatics.registry.neoforge;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.ithundxr.createnumismatics.registry.NumismaticsCreativeModeTabs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NumismaticsCreativeModeTabsRegistrateDisplayItemsGeneratorImpl {
    public static DeferredHolder<CreativeModeTab, CreativeModeTab> getTabObject(ResourceKey<CreativeModeTab> tab) {
        DeferredHolder<CreativeModeTab, CreativeModeTab> tabObject;
        if (tab == NumismaticsCreativeModeTabs.getBaseTabKey()) {
            tabObject = NumismaticsCreativeModeTabsImpl.MAIN_TAB;
        } else {
            tabObject = NumismaticsCreativeModeTabsImpl.MAIN_TAB;
        }
        return tabObject;
    }

    public static boolean isInCreativeTab(RegistryEntry<?, ?> entry, ResourceKey<CreativeModeTab> tab) {
        return CreateRegistrate.isInCreativeTab(entry, getTabObject(tab));
    }
}
