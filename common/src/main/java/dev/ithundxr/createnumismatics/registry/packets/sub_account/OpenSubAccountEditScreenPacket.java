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

import dev.ithundxr.createnumismatics.content.bank.SubAccountListMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record OpenSubAccountEditScreenPacket(@Nullable UUID subAccountID) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, OpenSubAccountEditScreenPacket> STREAM_CODEC =
        CatnipStreamCodecBuilders.nullable(UUIDUtil.STREAM_CODEC).map(
            OpenSubAccountEditScreenPacket::new,
            OpenSubAccountEditScreenPacket::subAccountID
        );

    @Override
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof SubAccountListMenu subAccountListMenu) {
            subAccountListMenu.openSubAccountEditScreen(subAccountID);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.OPEN_SUB_ACCOUNT_EDIT_SCREEN;
    }
}
