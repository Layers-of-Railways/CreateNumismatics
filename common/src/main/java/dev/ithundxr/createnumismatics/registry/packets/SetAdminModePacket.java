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

import dev.ithundxr.createnumismatics.mixin_interfaces.IAdminModePlayer;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SetAdminModePacket(boolean adminMode) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, SetAdminModePacket> STREAM_CODEC = ByteBufCodecs.BOOL.map(
        SetAdminModePacket::new,
        SetAdminModePacket::adminMode
    );

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player instanceof IAdminModePlayer adminModePlayer)
            adminModePlayer.numismatics$setAdminMode(adminMode);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.SET_ADMIN_MODE;
    }
}
