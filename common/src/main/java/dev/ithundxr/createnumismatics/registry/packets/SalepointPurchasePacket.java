/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

import dev.ithundxr.createnumismatics.content.backend.IDeductable;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointBlockEntity;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointPurchaseMenu;
import dev.ithundxr.createnumismatics.multiloader.C2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * @param multiplier If 0, cancel the current transaction.
 */
public record SalepointPurchasePacket(int multiplier) implements C2SPacket {

    public SalepointPurchasePacket(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(multiplier);
    }

    @Override
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof SalepointPurchaseMenu salepointPurchaseMenu) {
            SalepointBlockEntity salepointBE = salepointPurchaseMenu.contentHolder;

            if (multiplier == 0) {
                salepointBE.cancelTransaction();
                return;
            }

            IDeductable deductable = IDeductable.get(salepointPurchaseMenu.getCard(), sender, ReasonHolder.IGNORED);
            if (deductable == null)
                return;

            salepointBE.startTransaction(deductable, multiplier);
        }
    }
}
