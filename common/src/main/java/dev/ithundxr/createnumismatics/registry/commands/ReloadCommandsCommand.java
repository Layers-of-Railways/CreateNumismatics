/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.registry.NumismaticsCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class ReloadCommandsCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        return literal("reload_commands")
            .requires(cs -> cs.hasPermission(2))
            .executes(ctx -> {

                for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                    ctx.getSource().getServer().getCommands().sendCommands(player);
                }
                NumismaticsCommands.register(dispatcher, dedicated);
                ctx.getSource().sendSuccess(() -> Components.literal("Reloaded commands!"), true);
                return 1;
            });
    }
}
