package dev.ithundxr.createnumismatics.forge;

import com.mojang.brigadier.CommandDispatcher;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

@Mod(Numismatics.MOD_ID)
@Mod.EventBusSubscriber
public class NumismaticsImpl {
    public NumismaticsImpl() {
        // registrate must be given the mod event bus on forge before registration
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        NumismaticsBlocks.REGISTRATE.registerEventListeners(eventBus);
        Numismatics.init();
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
