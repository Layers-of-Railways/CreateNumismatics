/*
 * Numismatics
 * Copyright (c) 2024-2026 The Railways Team
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

package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.content.backend.IGhostItemMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record GhostItemSubmitPacket(int slot, ItemStack stack) implements ServerboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, GhostItemSubmitPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, GhostItemSubmitPacket::slot,
        ItemStack.OPTIONAL_STREAM_CODEC, GhostItemSubmitPacket::stack,
        GhostItemSubmitPacket::new
    );

    @Override
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof IGhostItemMenu ghostItemMenu) {
            if (ghostItemMenu.isSlotGhost(slot)) {
                ghostItemMenu.setGhostStackInSlot(slot, stack);
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.GHOST_ITEM_SUBMIT;
    }
}
