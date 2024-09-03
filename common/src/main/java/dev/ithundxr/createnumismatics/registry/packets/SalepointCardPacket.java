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

import dev.ithundxr.createnumismatics.content.salepoint.SalepointPurchaseScreen;
import dev.ithundxr.createnumismatics.multiloader.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record SalepointCardPacket(@Nullable Component message, int maxWithdrawal, @Nullable Component stateMessage) implements S2CPacket {
    public SalepointCardPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean() ? buf.readComponent() : null, buf.readVarInt(), buf.readBoolean() ? buf.readComponent() : null);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(message != null);
        if (message != null)
            buffer.writeComponent(message);
        buffer.writeVarInt(maxWithdrawal);
        buffer.writeBoolean(stateMessage != null);
        if (stateMessage != null)
            buffer.writeComponent(stateMessage);
    }

    @Override
    public void handle(Minecraft mc) {
        if (mc.screen instanceof SalepointPurchaseScreen salepointPurchaseScreen) {
            salepointPurchaseScreen.getMenu().serverSentCardMessage = message;
            salepointPurchaseScreen.getMenu().serverSentMaxWithdrawal = maxWithdrawal;
            salepointPurchaseScreen.getMenu().serverSentStateMessage = stateMessage;
        }
    }
}
