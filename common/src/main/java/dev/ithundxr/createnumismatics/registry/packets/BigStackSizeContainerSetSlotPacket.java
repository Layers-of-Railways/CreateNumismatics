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

import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

// todo check that extra stack size still works, apply PR
public record BigStackSizeContainerSetSlotPacket(int containerId, int stateId, int slot, ItemStack itemStack) implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, BigStackSizeContainerSetSlotPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE, i -> (byte) i.containerId,
        ByteBufCodecs.VAR_INT, BigStackSizeContainerSetSlotPacket::stateId,
        ByteBufCodecs.SHORT, i -> (short) i.slot,
        ItemStack.STREAM_CODEC, BigStackSizeContainerSetSlotPacket::itemStack,
        (containerId, stateId, slot, itemStack) -> new BigStackSizeContainerSetSlotPacket(containerId, stateId, slot, itemStack)
    );

    @Environment(EnvType.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        // IntelliJ falsely thinks that player.containerMenu is never null
        //noinspection ConstantValue
        if (player.containerMenu != null && player.containerMenu.containerId == containerId) {
            player.containerMenu.setItem(slot, stateId, itemStack);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        // fixme rename
        return NumismaticsPackets.VENDOR_CONTAINER_SET_SLOT;
    }
}
