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

public record VendorContainerSetContentPacket(int containerId, int stateId, List<ItemStack> items, ItemStack carriedItem) implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, VendorContainerSetContentPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE, i -> (byte) i.containerId,
        ByteBufCodecs.VAR_INT, VendorContainerSetContentPacket::stateId,
        CatnipStreamCodecBuilders.nonNullList(ItemStack.OPTIONAL_STREAM_CODEC), p -> {
            NonNullList<ItemStack> newList = NonNullList.withSize(p.items.size(), ItemStack.EMPTY);
            for (int i = 0; i < p.items.size(); ++i)
               newList.set(i, p.items.get(i).copy());
            return newList;
        },
        ItemStack.OPTIONAL_STREAM_CODEC, VendorContainerSetContentPacket::carriedItem,
        (containerId, stateId, items, carriedItem) -> new VendorContainerSetContentPacket(containerId, stateId, items, carriedItem)
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
        return NumismaticsPackets.VENDOR_CONTAINER_SET_CONTENT;
    }
}
