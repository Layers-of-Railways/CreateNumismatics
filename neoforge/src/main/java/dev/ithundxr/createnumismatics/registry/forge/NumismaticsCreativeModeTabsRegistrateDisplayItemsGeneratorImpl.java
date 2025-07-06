package dev.ithundxr.createnumismatics.registry.forge;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsCreativeModeTabs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.RegistryObject;

public class NumismaticsCreativeModeTabsRegistrateDisplayItemsGeneratorImpl {

    public static RegistryObject<CreativeModeTab> getTabObject(ResourceKey<CreativeModeTab> tab) {
        RegistryObject<CreativeModeTab> tabObject;
        if (tab == NumismaticsCreativeModeTabs.getBaseTabKey()) {
            tabObject = NumismaticsCreativeModeTabsImpl.MAIN_TAB;
        } else {
            tabObject = NumismaticsCreativeModeTabsImpl.MAIN_TAB;
        }
        return tabObject;
    }

    public static boolean isInCreativeTab(RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        return CreateRegistrate.isInCreativeTab(entry, getTabObject(tab));
    }
}
