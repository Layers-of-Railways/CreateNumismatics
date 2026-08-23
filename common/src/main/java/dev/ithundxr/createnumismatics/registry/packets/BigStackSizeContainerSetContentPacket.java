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

import dev.ithundxr.createnumismatics.content.vendor.VendorMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// todo check that this actually still handles big stack sizes correctly
public record BigStackSizeContainerSetContentPacket(int containerId, int stateId, List<ItemStack> items, ItemStack carriedItem) implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, BigStackSizeContainerSetContentPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE, i -> (byte) i.containerId,
        ByteBufCodecs.VAR_INT, BigStackSizeContainerSetContentPacket::stateId,
        CatnipStreamCodecBuilders.nonNullList(ItemStack.OPTIONAL_STREAM_CODEC), p -> {
            NonNullList<ItemStack> newList = NonNullList.withSize(p.items.size(), ItemStack.EMPTY);
            for (int i = 0; i < p.items.size(); ++i)
               newList.set(i, p.items.get(i).copy());
            return newList;
        },
        ItemStack.OPTIONAL_STREAM_CODEC, BigStackSizeContainerSetContentPacket::carriedItem,
        (containerId, stateId, items, carriedItem) -> new BigStackSizeContainerSetContentPacket(containerId, stateId, items, carriedItem)
    );

    @Environment(EnvType.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        // IntelliJ falsely thinks that player.containerMenu is never null
        //noinspection ConstantValue
        if (player.containerMenu != null && player.containerMenu instanceof VendorMenu && player.containerMenu.containerId == containerId) {
            player.containerMenu.initializeContents(stateId, items, carriedItem);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        // fixme rename
        return NumismaticsPackets.VENDOR_CONTAINER_SET_CONTENT;
    }
}
