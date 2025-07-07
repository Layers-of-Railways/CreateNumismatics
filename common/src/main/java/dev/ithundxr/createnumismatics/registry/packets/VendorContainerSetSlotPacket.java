package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.content.vendor.VendorMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record VendorContainerSetSlotPacket(int containerId, int stateId, int slot, ItemStack itemStack) implements ClientboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, VendorContainerSetSlotPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE, i -> (byte) i.containerId,
        ByteBufCodecs.VAR_INT, VendorContainerSetSlotPacket::stateId,
        ByteBufCodecs.SHORT, i -> (short) i.slot, 
        ItemStack.STREAM_CODEC, VendorContainerSetSlotPacket::itemStack,
        (containerId, stateId, slot, itemStack) -> new VendorContainerSetSlotPacket(containerId, stateId, slot, itemStack)
    );

    @Environment(EnvType.CLIENT)
    @Override
    public void handle(LocalPlayer player) {
        // IntelliJ falsely thinks that player.containerMenu is never null
        //noinspection ConstantValue
        if (player.containerMenu != null && player.containerMenu instanceof VendorMenu && player.containerMenu.containerId == containerId) {
            player.containerMenu.setItem(slot, stateId, itemStack);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.VENDOR_CONTAINER_SET_SLOT;
    }
}
