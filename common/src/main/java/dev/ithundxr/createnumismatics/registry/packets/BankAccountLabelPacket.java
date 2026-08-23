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

package dev.ithundxr.createnumismatics.registry.packets;

import com.mojang.serialization.Codec;
import dev.ithundxr.createnumismatics.NumismaticsClient;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.SubAccount;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public record BankAccountLabelPacket(boolean isSubAccount, UUID id, @Nullable String label) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, BankAccountLabelPacket> STREAM_CODEC = StreamCodec.composite(
        Codec.BOOL, BankAccountLabelPacket::isSubAccount,
        UUIDUtil.STREAM_CODEC, BankAccountLabelPacket::id,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.STRING_UTF8), BankAccountLabelPacket::label,
        BankAccountLabelPacket::new
    );

    public BankAccountLabelPacket(BankAccount account) {
        this(false, account.id, account.getLabel());
    }

    public BankAccountLabelPacket(SubAccount subAccount) {
        this(true, subAccount.getAuthorizationID(), subAccount.getLabel());
    }

    public static BankAccountLabelPacket remove(BankAccount account) {
        return new BankAccountLabelPacket(false, account.id, null);
    }

    public static BankAccountLabelPacket remove(SubAccount subAccount) {
        return new BankAccountLabelPacket(true, subAccount.getAuthorizationID(), null);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        Map<UUID, String> labelMap = isSubAccount ? NumismaticsClient.subAccountLabels : NumismaticsClient.bankAccountLabels;
        if (label == null) {
            labelMap.remove(id);
        } else {
            labelMap.put(id, label);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.BANK_ACCOUNT_LABEL;
    }
}
