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

import dev.ithundxr.createnumismatics.content.salepoint.SalepointConfigMenu;
import dev.ithundxr.createnumismatics.content.salepoint.states.EnergySalepointState;
import dev.ithundxr.createnumismatics.content.salepoint.types.Energy;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record SalepointEnergyFilterPacket(Energy filter) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, SalepointEnergyFilterPacket> STREAM_CODEC = Energy.STREAM_CODEC.map(
        SalepointEnergyFilterPacket::new,
        SalepointEnergyFilterPacket::filter
    );

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof SalepointConfigMenu salepointConfigMenu) {
            if (salepointConfigMenu.getSalepointState() instanceof EnergySalepointState energySalepointState) {
                energySalepointState.setFilter(filter, salepointConfigMenu.contentHolder.getLevel(), salepointConfigMenu.contentHolder.getBlockPos(), sender);
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.SALEPOINT_ENERGY_FILTER;
    }
}
