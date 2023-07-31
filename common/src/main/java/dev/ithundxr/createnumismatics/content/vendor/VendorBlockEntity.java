package dev.ithundxr.createnumismatics.content.vendor;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VendorBlockEntity extends SmartBlockEntity implements Trusted {
    public VendorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    protected UUID owner;

    protected final List<UUID> trustList = new ArrayList<>();

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (owner != null)
            tag.putUUID("Owner", owner);

//        if (!inventory.isEmpty()) {
//            tag.put("Inventory", inventory.save(new CompoundTag()));
//        }
//
//        if (!cardContainer.getItem(0).isEmpty()) {
//            tag.put("Card", cardContainer.getItem(0).save(new CompoundTag()));
//        }
//
//        if (!trustListContainer.isEmpty()) {
//            tag.put("TrustListInv", trustListContainer.save(new CompoundTag()));
//        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;

//        inventory.clear();
//        if (tag.contains("Inventory", Tag.TAG_COMPOUND)) {
//            inventory.load(tag.getCompound("Inventory"));
//        }
//
//        if (tag.contains("Card", Tag.TAG_COMPOUND)) {
//            ItemStack cardStack = ItemStack.of(tag.getCompound("Card"));
//            cardContainer.setItem(0, cardStack);
//        } else {
//            cardContainer.setItem(0, ItemStack.EMPTY);
//        }
//
//        trustListContainer.clearContent();
//        trustList.clear();
//        if (tag.contains("TrustListInv", Tag.TAG_COMPOUND)) {
//            trustListContainer.load(tag.getCompound("TrustListInv"));
//        }
    }

    @Override
    public boolean isTrustedInternal(Player player) {
        if (Utils.isDevEnv()) { // easier to test this way in dev
            return player.getItemBySlot(EquipmentSlot.FEET).is(Items.GOLDEN_BOOTS);
        } else {
            return owner == null || owner.equals(player.getUUID()) || trustList.contains(player.getUUID());
        }
    }
}
