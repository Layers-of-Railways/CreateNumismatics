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

package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.mixin_interfaces.IAdminModePlayer;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.literal;

public class ToggleAdminModeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("toggle_admin_mode")
            .requires(cs -> cs.hasPermission(2))
            .executes(ctx -> {
                if (ctx.getSource().getPlayerOrException() instanceof IAdminModePlayer adminModePlayer) {
                    boolean newAdminMode = !adminModePlayer.numismatics$isAdminMode();
                    adminModePlayer.numismatics$setAdminMode(newAdminMode);
                    ctx.getSource().sendSuccess(() -> Components.literal("Turned admin mode "+(newAdminMode ? "on" : "off")), true);
                    return 1;
                } else {
                    ctx.getSource().sendFailure(Components.literal("You are not a player!"));
                    return 0;
                }
            });
    }
}
