package dev.ithundxr.createnumismatics.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsAdvancements;
import dev.ithundxr.createnumismatics.registry.NumismaticsTriggers;
import dev.ithundxr.createnumismatics.registry.commands.arguments.EnumArgument;
import dev.ithundxr.createnumismatics.registry.neoforge.NumismaticsCreativeModeTabsImpl;
import dev.ithundxr.createnumismatics.registry.neoforge.NumismaticsDataComponentsImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.i18n.MavenVersionTranslator;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

@Mod(Numismatics.MOD_ID)
@EventBusSubscriber
public class NumismaticsImpl {
    static IEventBus modEventBus;

    public NumismaticsImpl(IEventBus modEventBus) {
        NumismaticsImpl.modEventBus = modEventBus;
        NumismaticsCreativeModeTabsImpl.register(modEventBus);
        NumismaticsDataComponentsImpl.register(modEventBus);
        Numismatics.init();

        modEventBus.addListener(NumismaticsImpl::registerArgumentTypes);
        modEventBus.addListener(NumismaticsImpl::onRegisterEvent);
        modEventBus.addListener(EventPriority.HIGHEST, NumismaticsDataNeoForge::gatherDataHighPriority);
        modEventBus.addListener(EventPriority.LOWEST, NumismaticsDataNeoForge::gatherData);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerArgumentTypes(RegisterEvent event) {
        event.register(Registries.COMMAND_ARGUMENT_TYPE, Numismatics.asResource("enum"),
            () -> ArgumentTypeInfos.registerByClass(EnumArgument.class, new EnumArgument.Info()));
    }
    
    public static void onRegisterEvent(RegisterEvent event) {
        if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES) {
            NumismaticsAdvancements.register();
            NumismaticsTriggers.register();
        }
    }

    public static String findVersion() {
        String versionString = "UNKNOWN";

        List<IModInfo> infoList = ModList.get().getModFileById(Numismatics.MOD_ID).getMods();
        if (infoList.size() > 1) {
            Numismatics.LOGGER.error("Multiple mods for MOD_ID: " + Numismatics.MOD_ID);
        }
        for (IModInfo info : infoList) {
            if (info.getModId().equals(Numismatics.MOD_ID)) {
                versionString = MavenVersionTranslator.artifactVersionToString(info.getVersion());
                break;
            }
        }
        return versionString;
    }

    public static void finalizeRegistrate() {
        Numismatics.registrate().registerEventListeners(modEventBus);
    }

    private static final Set<BiConsumer<CommandDispatcher<CommandSourceStack>, Boolean>> commandConsumers = new HashSet<>();

    public static void registerCommands(BiConsumer<CommandDispatcher<CommandSourceStack>, Boolean> consumer) {
        commandConsumers.add(consumer);
    }

    @SubscribeEvent
    public static void onCommandRegistration(RegisterCommandsEvent event) {
        CommandSelection selection = event.getCommandSelection();
        boolean dedicated = selection == CommandSelection.ALL || selection == CommandSelection.DEDICATED;
        commandConsumers.forEach(consumer -> consumer.accept(event.getDispatcher(), dedicated));
    }
}
