/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
