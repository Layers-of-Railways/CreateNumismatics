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

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TAB_REGISTER.register("main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.numismatics"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(NumismaticsBlocks.VENDOR::asStack)
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
