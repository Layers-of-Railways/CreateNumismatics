/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
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

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.codec.NumismaticsStreamCodecs;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.util.Utils;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public record OpenSubAccountsMenuPacket(UUID accountID, boolean open) implements ServerboundPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, OpenSubAccountsMenuPacket> STREAM_CODEC = StreamCodec.composite(
        NumismaticsStreamCodecs.UUID, OpenSubAccountsMenuPacket::accountID,
        ByteBufCodecs.BOOL, OpenSubAccountsMenuPacket::open,
        OpenSubAccountsMenuPacket::new
    );

    public OpenSubAccountsMenuPacket(BankAccount bankAccount) {
        this(bankAccount, true);
    }

    public OpenSubAccountsMenuPacket(BankAccount bankAccount, boolean open) {
        this(bankAccount.id, open);
    }

    @Override
    public void handle(ServerPlayer sender) {
        BankAccount account = Numismatics.BANK.getAccount(accountID);
        if (account != null && account.isAuthorized(sender)) {
            if (open) {
                account.openSubAccountsMenu(sender);
            } else {
                Utils.openScreen(sender, account, account::sendToMenu);
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.OPEN_SUB_ACCOUNTS_MENU;
    }
}
