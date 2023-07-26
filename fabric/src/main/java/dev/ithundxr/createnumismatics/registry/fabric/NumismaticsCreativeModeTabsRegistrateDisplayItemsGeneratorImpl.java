package dev.ithundxr.createnumismatics.registry.fabric;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class NumismaticsCreativeModeTabsRegistrateDisplayItemsGeneratorImpl {
    public static boolean isInCreativeTab(RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        return Numismatics.registrate().isInCreativeTab(entry, tab);
    }
}
