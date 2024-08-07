/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.content.salepoint.states;

import com.simibubi.create.foundation.utility.Components;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.compat.computercraft.ComputerCraftProxy;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.SalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableAbstractBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableWrappingItemBuffer;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemSalepointState implements ISalepointState<ItemStack> {

    private UUID uuid;
    private @NotNull ItemStack filter = ItemStack.EMPTY;
    private final @NotNull SimpleContainer buffer = new SimpleContainer(4) {
        @Override
        public ItemStack addItem(ItemStack stack) {
            if (!VendorBlockEntity.matchesFilterItem(filter, stack))
                return stack;

            return super.addItem(stack);
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return super.canPlaceItem(index, stack) && VendorBlockEntity.matchesFilterItem(filter, stack);
        }
    };
    private @NotNull InvalidatableWrappingItemBuffer bufferWrapper = createBufferWrapper(buffer);
    private @Nullable Runnable changedCallback;

    ItemSalepointState() {
        buffer.addListener($ -> setChanged());
    }

    @ExpectPlatform
    private static InvalidatableWrappingItemBuffer createBufferWrapper(SimpleContainer buffer) {
        throw new AssertionError();
    }

    @Override
    public void init() {
        uuid = UUID.randomUUID();
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    @Override
    public SalepointTypes getType() {
        return SalepointTypes.ITEM;
    }

    @Override
    public InvalidatableAbstractBuffer<ItemStack> getBuffer() {
        return bufferWrapper;
    }

    @Override
    public void onDestroy(Level level, BlockPos pos) {
        onUnload();
        Containers.dropContents(level, pos, buffer);
    }

    @Override
    public void onUnload() {
        bufferWrapper.invalidate();
    }

    @Override
    public void keepAlive() {
        if (!bufferWrapper.isValid()) {
            bufferWrapper = createBufferWrapper(buffer);
        }
    }

    @Override
    public boolean canChangeFilterTo(ItemStack filter) {
        return filter.getCount() <= filter.getMaxStackSize();
    }

    @Override
    public boolean setFilter(ItemStack filter, Level salepointLevel, BlockPos salepointPos, @Nullable Player player) {
        if (!canChangeFilterTo(filter))
            return false;

        this.filter = filter.copy();
        NonNullList<ItemStack> invalidatedStacks = cleanBuffer();
        if (player != null) {
            for (ItemStack stack : invalidatedStacks) {
                player.getInventory().placeItemBackInInventory(stack);
            }
        } else {
            Containers.dropContents(salepointLevel, salepointPos.above(), invalidatedStacks);
        }
        setChanged();

        return true;
    }

    @Override
    public @NotNull ItemStack getFilter() {
        return filter.copy();
    }

    @Override
    public boolean filterMatches(ItemStack object) {
        return VendorBlockEntity.matchesFilterItem(filter, object);
    }

    @Override
    public boolean configGuiHasFilterSlot() {
        return true;
    }

    @Override
    public boolean purchaseGuiHasDisplaySlot() {
        return true;
    }

    private static final Container emptyContainer = new SimpleContainer(0);

    @Override
    public Slot createConfigGuiFilterSlot(Level salepointLevel, BlockPos salepointPos, @Nullable Player player) {
        return new Slot(emptyContainer, 0, 106, 60) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return canChangeFilterTo(stack);
            }

            @Override
            public ItemStack getItem() {
                return filter;
            }

            @Override
            public void set(ItemStack stack) {
                setFilter(stack, salepointLevel, salepointPos, player);
            }

            @Override
            public void setChanged() {
                ItemSalepointState.this.setChanged();
            }

            @Override
            public int getMaxStackSize() {
                return Integer.MAX_VALUE;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return stack.getMaxStackSize();
            }

            @Override
            public ItemStack remove(int amount) {
                amount = Math.min(amount, getItem().getCount());
                ItemStack out = getItem().split(amount);
                setChanged();
                return out;
            }
        };
    }

    @Override
    public Slot createPurchaseGuiDisplaySlot(Level salepointLevel, BlockPos salepointPos, @Nullable Player player) {
        return new Slot(emptyContainer, 0, 74, 61) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public ItemStack getItem() {
                return filter;
            }

            @Override
            public void set(ItemStack stack) {
                // no-op
            }

            @Override
            public void setChanged() {
                // no-op
            }

            @Override
            public int getMaxStackSize() {
                return Integer.MAX_VALUE;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return stack.getMaxStackSize();
            }

            @Override
            public ItemStack remove(int amount) {
                return ItemStack.EMPTY;
            }
        };
    }

    protected NonNullList<ItemStack> cleanBuffer() {
        compactBuffer();

        NonNullList<ItemStack> invalid = NonNullList.createWithCapacity(4);
        for (int i = 0; i < buffer.getContainerSize(); i++) {
            ItemStack stack = buffer.getItem(i);
            if (!stack.isEmpty() && !VendorBlockEntity.matchesFilterItem(filter, stack)) {
                invalid.add(stack);
                buffer.setItem(i, ItemStack.EMPTY);
            }
        }

        compactBuffer();

        return invalid;
    }

    protected void compactBuffer() {
        for (int i = 0; i < buffer.getContainerSize(); i++) {
            ItemStack stack = buffer.getItem(i);

            if (!stack.isEmpty() && !stack.isStackable())
                continue;

            if (stack.getCount() >= stack.getMaxStackSize())
                continue;

            for (int j = i; j < buffer.getContainerSize(); j++) {
                ItemStack other = buffer.getItem(j);
                if (other.isEmpty())
                    continue;

                if (stack.isEmpty() || ItemStack.isSameItemSameTags(stack, other)) {
                    // do merging
                    int space = other.getMaxStackSize() - stack.getCount();
                    int transfer = Math.min(space, other.getCount());
                    if (transfer <= 0)
                        continue;
                    stack.grow(transfer);
                    other.shrink(transfer);
                    buffer.setItem(i, stack);
                    buffer.setItem(j, other);
                }
            }
        }
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", getType().getId());
        tag.putUUID("UUID", uuid);

        // save buffer
        ListTag bufferTag = new ListTag();
        for (int i = 0; i < buffer.getContainerSize(); i++) {
            ItemStack stack = buffer.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                stack.save(stackTag);
                bufferTag.add(stackTag);
            }
        }
        tag.put("Buffer", bufferTag);

        if (!filter.isEmpty())
            tag.put("Filter", filter.save(new CompoundTag()));

        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        uuid = tag.getUUID("UUID");

        // load buffer
        buffer.clearContent();
        ListTag bufferTag = tag.getList("Buffer", Tag.TAG_COMPOUND);
        for (int i = 0; i < bufferTag.size(); i++) {
            CompoundTag stackTag = bufferTag.getCompound(i);
            buffer.setItem(i, ItemStack.of(stackTag));
        }

        if (tag.contains("Filter", Tag.TAG_COMPOUND))
            filter = ItemStack.of(tag.getCompound("Filter"));
        else
            filter = ItemStack.EMPTY;
    }

    @Override
    public boolean isValidForPurchase(Level level, BlockPos targetedPos, ReasonHolder reasonHolder) {
        SalepointTargetBehaviour<ItemStack> behaviour = getBehaviour(level, targetedPos);
        return isValidForPurchase(behaviour, reasonHolder);
    }

    private boolean hasBufferItemsForPurchase() {
        int count = filter.getCount();
        for (int i = 0; i < buffer.getContainerSize(); i++) {
            ItemStack stack = buffer.getItem(i);
            if (VendorBlockEntity.matchesFilterItem(filter, stack)) {
                count -= stack.getCount();
                if (count <= 0)
                    return true;
            }
        }

        return false;
    }

    private List<ItemStack> removeBufferItemsForPurchase() {
        compactBuffer();
        List<ItemStack> out = new ArrayList<>();
        int count = filter.getCount();
        for (int i = 0; i < buffer.getContainerSize(); i++) {
            ItemStack stack = buffer.getItem(i);
            if (VendorBlockEntity.matchesFilterItem(filter, stack)) {
                int transfer = Math.min(count, stack.getCount());
                count -= transfer;
                out.add(stack.split(transfer));
                buffer.setItem(i, stack);
                if (count <= 0)
                    return out;
            }
        }
        return out;
    }

    private boolean isValidForPurchase(@Nullable SalepointTargetBehaviour<ItemStack> behaviour, ReasonHolder reasonHolder) {
        if (behaviour == null) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.no_target"));
            return false;
        }

        if (!behaviour.isUnderControl(this)) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.target_not_controlled"));
            return false;
        }

        if (filter.isEmpty()) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.no_filter"));
            return false;
        }

        if (!hasBufferItemsForPurchase()) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.vendor.out_of_stock"));
            return false;
        }

        if (!behaviour.hasSpaceFor(filter.copy())) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.insufficient_space"));
            return false;
        }

        return true;
    }

    @Override
    public boolean doPurchase(Level level, BlockPos targetedPos, ReasonHolder reasonHolder) {
        SalepointTargetBehaviour<ItemStack> behaviour = getBehaviour(level, targetedPos);
        if (behaviour == null) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.no_target"));
            return false;
        }

        if (!isValidForPurchase(behaviour, reasonHolder))
            return false;

        if (!behaviour.doPurchase(filter.copy(), this::removeBufferItemsForPurchase)) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.target_failed_purchase"));
            return false;
        }

        return true;
    }

    @Override
    public void ensureUnderControl(Level level, BlockPos targetedPos) {
        SalepointTargetBehaviour<ItemStack> behaviour = getBehaviour(level, targetedPos);
        if (behaviour == null)
            return;

        behaviour.ensureUnderControl(this);
    }

    @Override
    public void relinquishControl(Level level, BlockPos targetedPos) {
        SalepointTargetBehaviour<ItemStack> behaviour = getBehaviour(level, targetedPos);
        if (behaviour == null)
            return;

        behaviour.relinquishControl(this);
    }

    @Override
    public void setChangedCallback(Runnable callback) {
        this.changedCallback = callback;
    }

    protected void setChanged() {
        if (changedCallback != null)
            changedCallback.run();
    }

    @Override
    public Map<String, Object> writeForComputerCraft() {
        return Map.of(
            "type", getType().getId(),
            "filter", ComputerCraftProxy.getItemDetail(filter)
        );
    }
}
