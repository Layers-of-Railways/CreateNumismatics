/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.config.neoforge.NumismaticsConfigImpl;
import dev.ithundxr.createnumismatics.registry.NumismaticsAdvancements;
import dev.ithundxr.createnumismatics.registry.NumismaticsTriggers;
import dev.ithundxr.createnumismatics.registry.commands.arguments.EnumArgument;
import dev.ithundxr.createnumismatics.registry.neoforge.NumismaticsCreativeModeTabsImpl;
import dev.ithundxr.createnumismatics.registry.neoforge.NumismaticsDataComponentsImpl;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

@Mod(Numismatics.MOD_ID)
@EventBusSubscriber
public class NumismaticsImpl {
    static IEventBus modEventBus;

    public NumismaticsImpl(IEventBus modEventBus, ModContainer modContainer) {
        restoreLoggers();
        NumismaticsImpl.modEventBus = modEventBus;
        NumismaticsCreativeModeTabsImpl.register(modEventBus);
        NumismaticsDataComponentsImpl.register(modEventBus);
        Numismatics.init();

        NumismaticsConfigImpl.register(ModLoadingContext.get(), modContainer);

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

    private static void restoreLoggers() {
        if (Utils.isDevEnv()) {

            if (Boolean.getBoolean("numismatics.dev.skipLoggerRestoration")) {
                return;
            }

            // restore our logging config, since forge likes to nuke it for fun
            for (String prop : new String[] {"log4j.configurationFile", "log4j2.configurationFile"}) {
                String fileStr = System.getProperty(prop);
                if (fileStr != null) {
                    File file = new File(fileStr);
                    if (file.exists() && file.isFile() && file.canRead()) {
                        ConfigurationSource source;
                        try {
                            source = new ConfigurationSource(new FileInputStream(file), file);
                        } catch (FileNotFoundException e) {
                            Numismatics.LOGGER.error("Failed to open log4j file", e);
                            continue;
                        }
                        Configurator.reconfigure(ConfigurationFactory.getInstance().getConfiguration(
                            LoggerContext.getContext(),
                            source
                        ));
                        break;
                    }
                }
            }
        }
    }
}
