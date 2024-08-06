/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.fabric;

import com.mojang.brigadier.CommandDispatcher;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.config.fabric.NumismaticsConfigImpl;
import dev.ithundxr.createnumismatics.events.fabric.CommonEventsFabric;
import dev.ithundxr.createnumismatics.registry.commands.arguments.EnumArgument;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

public class NumismaticsImpl implements ModInitializer {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void onInitialize() {
        Numismatics.init();
        NumismaticsConfigImpl.register();
        CommonEventsFabric.init();
        ArgumentTypeRegistry.registerArgumentType(new ResourceLocation(Numismatics.MOD_ID, "enum"), EnumArgument.class, new EnumArgument.Info());
    }

    public static String findVersion() {
        return FabricLoader.getInstance()
                .getModContainer(Numismatics.MOD_ID)
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }

    public static void finalizeRegistrate() {
        Numismatics.registrate().register();
        Numismatics.postRegistrationInit();
    }

    public static void registerCommands(BiConsumer<CommandDispatcher<CommandSourceStack>, Boolean> consumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> consumer.accept(dispatcher, environment.includeDedicated));
    }
}
