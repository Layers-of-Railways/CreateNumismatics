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
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.AuthorizationType;
import dev.ithundxr.createnumismatics.content.bank.SubAccountListMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ConfigureSubAccountPacket(
    @NotNull UUID subAccountID,
    @Nullable Integer limit,
    @Nullable AuthorizationType authorizationType,
    @Nullable String label
) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, ConfigureSubAccountPacket> STREAM_CODEC = StreamCodec.composite(
        NumismaticsStreamCodecs.UUID, ConfigureSubAccountPacket::subAccountID,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.VAR_INT), ConfigureSubAccountPacket::limit,
        CatnipStreamCodecBuilders.nullable(AuthorizationType.STREAM_CODEC), ConfigureSubAccountPacket::authorizationType,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.STRING_UTF8), ConfigureSubAccountPacket::label,
        ConfigureSubAccountPacket::new
    );

    public ConfigureSubAccountPacket(@NotNull UUID subAccountID, @Nullable Integer limit) {
        this(subAccountID, limit, null, null);
    }

    public ConfigureSubAccountPacket(@NotNull UUID subAccountID, @NotNull AuthorizationType authorizationType) {
        this(subAccountID, null, authorizationType, null);
    }

    public ConfigureSubAccountPacket(@NotNull UUID subAccountID, @NotNull String label) {
        this(subAccountID, null, null, label);
    }

    @Override
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof SubAccountListMenu subAccountListMenu) {
            if (limit != null)
                subAccountListMenu.setLimit(subAccountID, limit);

            if (authorizationType != null)
                subAccountListMenu.setAuthorizationType(subAccountID, authorizationType);

            if (label != null)
                subAccountListMenu.setLabel(subAccountID, label);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.CONFIGURE_SUB_ACCOUNT;
    }
}
