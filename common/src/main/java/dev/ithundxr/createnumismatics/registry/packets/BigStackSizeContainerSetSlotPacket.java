package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.content.vendor.VendorMenu;
import dev.ithundxr.createnumismatics.multiloader.S2CPacket;
import dev.ithundxr.createnumismatics.util.PacketUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BigStackSizeContainerSetSlotPacket implements S2CPacket {
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    public BigStackSizeContainerSetSlotPacket(int containerId, int stateId, int slot, ItemStack itemStack) {
        this.containerId = containerId;
        this.stateId = stateId;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    public BigStackSizeContainerSetSlotPacket(FriendlyByteBuf buffer) {
        containerId = buffer.readUnsignedByte();
        stateId = buffer.readVarInt();
        slot = buffer.readShort();
        itemStack = PacketUtils.readBigStackSizeItem(buffer);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(containerId);
        buffer.writeVarInt(stateId);
        buffer.writeShort(slot);
        PacketUtils.writeBigStackSizeItem(buffer, itemStack);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Minecraft mc) {
        Player player = mc.player;
        // IntelliJ falsely thinks that player.containerMenu is never null
        //noinspection ConstantValue,DataFlowIssue
        if (player.containerMenu != null && player.containerMenu instanceof VendorMenu && player.containerMenu.containerId == containerId) {
            player.containerMenu.setItem(slot, stateId, itemStack);
        }
    }
}
