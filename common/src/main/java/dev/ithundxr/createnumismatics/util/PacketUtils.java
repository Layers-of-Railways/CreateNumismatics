package dev.ithundxr.createnumismatics.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PacketUtils {
    /**
     * Reads an item without the 127 count limit
     */
    public static ItemStack readHighCountItem(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            Item item = buffer.readById(BuiltInRegistries.ITEM);
            int count = buffer.readInt();
            //noinspection DataFlowIssue
            ItemStack itemStack = new ItemStack(item, count);
            itemStack.setTag(buffer.readNbt());
            return itemStack;
        }
    }

    /**
     * Writes an item without the 127 count limit
     */
    public static void writeHighCountItem(FriendlyByteBuf buffer, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            Item item = itemStack.getItem();
            buffer.writeId(BuiltInRegistries.ITEM, item);
            buffer.writeInt(itemStack.getCount());
            CompoundTag compoundTag = null;
            if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
                compoundTag = itemStack.getTag();
            }

            buffer.writeNbt(compoundTag);
        }
    }
}
