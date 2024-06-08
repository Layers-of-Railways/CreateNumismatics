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

package dev.ithundxr.createnumismatics.events.fabric;

import dev.ithundxr.createnumismatics.events.CommonEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class CommonEventsFabric {
    public static void init() {
        ServerWorldEvents.LOAD.register((server, level) -> CommonEvents.onLoadWorld(level));
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) -> CommonEvents.onBlockBreak(level, pos, state, player));
        ServerPlayConnectionEvents.JOIN.register((connection, packetSender, server) -> CommonEvents.onPlayerJoin(connection.player));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> CommonEvents.onUseBlock(player, world, hand, hitResult));
    }
}
