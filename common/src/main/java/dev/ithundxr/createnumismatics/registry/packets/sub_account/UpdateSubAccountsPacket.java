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

package dev.ithundxr.createnumismatics.registry.packets.sub_account;

import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.bank.SubAccountListScreen;
import dev.ithundxr.createnumismatics.multiloader.S2CPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/** Only works for players who have a SubAccountListMenu open */
public class UpdateSubAccountsPacket implements S2CPacket {

    private final UUID accountID;
    private final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());;

    public UpdateSubAccountsPacket(BankAccount account) {
        this.accountID = account.id;
        account.sendSubAccountsOnlyToMenu(data);
    }

    public UpdateSubAccountsPacket(FriendlyByteBuf buf) {
        accountID = buf.readUUID();
        int length = buf.readVarInt();
        buf.readBytes(data, length);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(accountID);
        buffer.writeVarInt(data.readableBytes());
        buffer.writeBytes(data);
    }

    @Override
    public void handle(Minecraft mc) {
        if (mc.screen instanceof SubAccountListScreen sal) {
            BankAccount account = sal.getMenu().contentHolder;
            if (account.id.equals(accountID)) {
                account.updateSubAccountsFrom(data);
            }
        }
    }
}
