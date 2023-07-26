package dev.ithundxr.createnumismatics.forge;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.config.BackendTypeArgument;
import com.mojang.brigadier.CommandDispatcher;
import dev.ithundxr.createnumismatics.multiloader.Env;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.commands.arguments.EnumArgument;
import dev.ithundxr.createnumismatics.registry.forge.NumismaticsCreativeModeTabsImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.util.MavenVersionStringHelper;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.RegisterEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

@Mod(Numismatics.MOD_ID)
@Mod.EventBusSubscriber
public class NumismaticsImpl {
    static IEventBus eventBus;

    public NumismaticsImpl() {
        eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        NumismaticsCreativeModeTabsImpl.register(eventBus);
        Numismatics.init();
        //noinspection Convert2MethodRef
        Env.CLIENT.runIfCurrent(() -> () -> NumismaticsClientImpl.init());
        eventBus.addListener(NumismaticsImpl::registerArgumentTypes);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerArgumentTypes(RegisterEvent event) {
        event.register(Registries.COMMAND_ARGUMENT_TYPE, Numismatics.asResource("enum"),
            () -> ArgumentTypeInfos.registerByClass(EnumArgument.class, new EnumArgument.Info()));
    }

    public static String findVersion() {
        String versionString = "UNKNOWN";

        List<IModInfo> infoList = ModList.get().getModFileById(Numismatics.MOD_ID).getMods();
        if (infoList.size() > 1) {
            Numismatics.LOGGER.error("Multiple mods for MOD_ID: " + Numismatics.MOD_ID);
        }
        for (IModInfo info : infoList) {
            if (info.getModId().equals(Numismatics.MOD_ID)) {
                versionString = MavenVersionStringHelper.artifactVersionToString(info.getVersion());
                break;
            }
        }
        return versionString;
    }

    public static void finalizeRegistrate() {
        Numismatics.registrate().registerEventListeners(eventBus);
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
