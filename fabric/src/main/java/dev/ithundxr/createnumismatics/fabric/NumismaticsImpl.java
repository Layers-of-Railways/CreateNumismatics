package dev.ithundxr.createnumismatics.fabric;

import com.mojang.brigadier.CommandDispatcher;
import dev.ithundxr.createnumismatics.events.fabric.CommonEventsFabric;
import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.Numismatics;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.BiConsumer;

public class NumismaticsImpl implements ModInitializer {
    @Override
    public void onInitialize() {
        Numismatics.init();
        Numismatics.LOGGER.info(EnvExecutor.unsafeRunForDist(
                () -> () -> "{} is accessing Porting Lib on a Fabric client!",
                () -> () -> "{} is accessing Porting Lib on a Fabric server!"
                ), Numismatics.NAME);
        // on fabric, Registrates must be explicitly finalized and registered.
        NumismaticsBlocks.REGISTRATE.register();
        CommonEventsFabric.init();
    }

    public static void registerCommands(BiConsumer<CommandDispatcher<CommandSourceStack>, Boolean> consumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> consumer.accept(dispatcher, environment.includeDedicated));
    }
}
