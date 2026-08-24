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

import dev.ithundxr.createnumismatics.content.salepoint.SalepointPurchaseScreen;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

public record SalepointCardPacket(
    @Nullable Component message,
    int maxWithdrawal,
    @Nullable Component stateMessage
) implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SalepointCardPacket> STREAM_CODEC = StreamCodec.composite(
        CatnipStreamCodecBuilders.nullable(ComponentSerialization.STREAM_CODEC), SalepointCardPacket::message,
        ByteBufCodecs.VAR_INT, SalepointCardPacket::maxWithdrawal,
        CatnipStreamCodecBuilders.nullable(ComponentSerialization.STREAM_CODEC), SalepointCardPacket::stateMessage,
        SalepointCardPacket::new
    );

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen instanceof SalepointPurchaseScreen salepointPurchaseScreen) {
            salepointPurchaseScreen.getMenu().serverSentCardMessage = message;
            salepointPurchaseScreen.getMenu().serverSentMaxWithdrawal = maxWithdrawal;
            salepointPurchaseScreen.getMenu().serverSentStateMessage = stateMessage;
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.SALEPOINT_CARD;
    }
}
