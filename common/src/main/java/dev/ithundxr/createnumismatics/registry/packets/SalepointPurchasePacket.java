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

import dev.ithundxr.createnumismatics.content.backend.IAuthorizationCheckingDeductable;
import dev.ithundxr.createnumismatics.content.backend.IDeductable;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointBlockEntity;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointPurchaseMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

/**
 * @param multiplier If 0, cancel the current transaction.
 */
public record SalepointPurchasePacket(int multiplier) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, SalepointPurchasePacket> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
        SalepointPurchasePacket::new,
        SalepointPurchasePacket::multiplier
    );

    @Override
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof SalepointPurchaseMenu salepointPurchaseMenu) {
            SalepointBlockEntity salepointBE = salepointPurchaseMenu.contentHolder;

            if (multiplier == 0) {
                salepointBE.cancelTransaction();
                return;
            }

            IAuthorizationCheckingDeductable deductable = IDeductable.getAuthorizationChecking(salepointPurchaseMenu.getCard(), sender, ReasonHolder.IGNORED);
            if (deductable == null)
                return;

            salepointBE.startTransaction(deductable, multiplier);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.SALEPOINT_PURCHASE;
    }
}
