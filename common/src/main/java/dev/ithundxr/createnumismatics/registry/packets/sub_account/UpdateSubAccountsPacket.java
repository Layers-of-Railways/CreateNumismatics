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

package dev.ithundxr.createnumismatics.registry.packets.sub_account;

import dev.ithundxr.createnumismatics.base.codec.NumismaticsStreamCodecs;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.bank.SubAccountListScreen;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.Unpooled;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/** Only works for players who have a SubAccountListMenu open */
public class UpdateSubAccountsPacket implements ClientboundPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, UpdateSubAccountsPacket> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, i -> i.accountID,
        NumismaticsStreamCodecs.NESTED_BUF, i -> i.data,
        UpdateSubAccountsPacket::new
    );

    private final UUID accountID;
    private final FriendlyByteBuf data;

    private UpdateSubAccountsPacket(UUID accountID, FriendlyByteBuf data) {
        this.accountID = accountID;
        this.data = data;
    }

    public UpdateSubAccountsPacket(BankAccount account) {
        this(account.id, new FriendlyByteBuf(Unpooled.buffer()));
        account.sendSubAccountsOnlyToMenu(data);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen instanceof SubAccountListScreen sal) {
            BankAccount account = sal.getMenu().contentHolder;
            if (account.id.equals(accountID)) {
                account.updateSubAccountsFrom(data, player.registryAccess());
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.UPDATE_SUB_ACCOUNTS;
    }
}
