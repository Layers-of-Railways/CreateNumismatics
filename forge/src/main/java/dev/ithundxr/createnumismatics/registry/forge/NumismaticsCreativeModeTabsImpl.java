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

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsCreativeModeTabs.RegistrateDisplayItemsGenerator;
import dev.ithundxr.createnumismatics.registry.NumismaticsCreativeModeTabs.Tabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(bus = Bus.MOD)
public class NumismaticsCreativeModeTabsImpl {
    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Numismatics.MOD_ID);

    @SuppressWarnings("Convert2MethodRef")
    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TAB_REGISTER.register("main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.numismatics"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> NumismaticsBlocks.VENDOR.asStack())
            .displayItems(new RegistrateDisplayItemsGenerator(Tabs.MAIN))
            .build());

    public static void register(IEventBus modEventBus) {
        TAB_REGISTER.register(modEventBus);
    }

    public static CreativeModeTab getBaseTab() {
        return MAIN_TAB.get();
    }

    public static ResourceKey<CreativeModeTab> getBaseTabKey() {
        return MAIN_TAB.getKey();
    }
}
