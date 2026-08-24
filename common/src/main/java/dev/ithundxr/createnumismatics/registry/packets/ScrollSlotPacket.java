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

import dev.ithundxr.createnumismatics.content.backend.IScrollableSlotMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record ScrollSlotPacket(int slot, double delta, boolean shiftHeld) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, ScrollSlotPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ScrollSlotPacket::slot,
        ByteBufCodecs.DOUBLE, ScrollSlotPacket::delta,
        ByteBufCodecs.BOOL, ScrollSlotPacket::shiftHeld,
        ScrollSlotPacket::new
    );

    @Override
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof IScrollableSlotMenu scrollableMenu) {
            scrollableMenu.scrollSlot(slot, delta, shiftHeld);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.SCROLL_SLOT;
    }
}
