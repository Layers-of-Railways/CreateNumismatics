/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.bank.CardSlot;
import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
import dev.ithundxr.createnumismatics.content.bank.IDCardSlot.BoundIDCardSlot;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class BlazeBankerMenu extends MenuBase<BlazeBankerBlockEntity> {
    public static final int ID_CARD_SLOTS = 27;
    public static final int CARD_SLOT_INDEX = ID_CARD_SLOTS;
    public static final int PLAYER_INV_START_INDEX = CARD_SLOT_INDEX + 1;
    public static final int PLAYER_HOTBAR_END_INDEX = PLAYER_INV_START_INDEX + 9;
    public static final int PLAYER_INV_END_INDEX = PLAYER_INV_START_INDEX + 36;

    private CardWritingContainer cardWritingContainer;

    public BlazeBankerMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    protected BlazeBankerMenu(MenuType<?> type, int id, Inventory inv, BlazeBankerBlockEntity contentHolder) {
        super(type, id, inv, contentHolder);
    }

    @Override
    protected BlazeBankerBlockEntity createOnClient(FriendlyByteBuf extraData) {
        ClientLevel world = Minecraft.getInstance().level;
        BlockEntity blockEntity = world.getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof BlazeBankerBlockEntity blazeBankerBE) {
            blazeBankerBE.readClient(extraData.readNbt());
            return blazeBankerBE;
        }
        return null;
    }

    @Override
    protected void initAndReadInventory(BlazeBankerBlockEntity contentHolder) {}

    @Override
    protected void addSlots() {
        if (cardWritingContainer == null)
            cardWritingContainer = new CardWritingContainer(this::slotsChanged, contentHolder.getAccountId());
        int x = 16;
        int y = 21;

        for (int i = 0; i < ID_CARD_SLOTS; i++) {
            if (i % 9 == 0 && i > 0) {
                x = 16;
                y += 18;
            }
            addSlot(new BoundIDCardSlot(contentHolder.getTrustListBackingContainer(), i, x, y));
            x += 18;
        }
        addSlot(new CardSlot.UnboundCardSlot(cardWritingContainer, 0, 7, 87));

        addPlayerSlots(40, 130);
    }

    @Override
    protected void saveData(BlazeBankerBlockEntity contentHolder) {}

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (playerIn instanceof ServerPlayer) {
            clearContainer(player, cardWritingContainer);
        }
    }

    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot clickedSlot = this.slots.get(index);

        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack slotStack = CoinItem.clearDisplayedCount(clickedSlot.getItem());
        ItemStack returnStack = slotStack.copy();

        if (index <= CARD_SLOT_INDEX) {
            int count = slotStack.getCount();
            if (!moveItemStackTo(slotStack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, false))
                return ItemStack.EMPTY;

            returnStack = ItemStack.EMPTY;
            clickedSlot.remove(count);
        } else if (slotStack.getItem() instanceof IDCardItem && IDCardItem.isBound(slotStack) && !moveItemStackTo(slotStack, 0, ID_CARD_SLOTS, false)) {
            return ItemStack.EMPTY; // failed to move to id card slots
        } else if (NumismaticsTags.AllItemTags.CARDS.matches(slotStack) && !moveItemStackTo(slotStack, CARD_SLOT_INDEX, CARD_SLOT_INDEX + 1, false)) {
            return ItemStack.EMPTY; // failed to move to card slot
        } else if (index >= PLAYER_INV_START_INDEX && index < PLAYER_HOTBAR_END_INDEX && !moveItemStackTo(slotStack, PLAYER_HOTBAR_END_INDEX, PLAYER_INV_END_INDEX, false)) {
            return ItemStack.EMPTY;
        } else if (index >= PLAYER_HOTBAR_END_INDEX && index < PLAYER_INV_END_INDEX && !moveItemStackTo(slotStack, PLAYER_INV_START_INDEX, PLAYER_HOTBAR_END_INDEX, false)) {
            return ItemStack.EMPTY;
        }

        if (slotStack.isEmpty()) {
            clickedSlot.set(ItemStack.EMPTY);
        } else {
            clickedSlot.setChanged();
        }

        return returnStack;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        ItemStack itemStack;
        Slot slot;
        boolean bl = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }
        if (stack.isStackable() && startIndex >= ID_CARD_SLOTS) {
            while (!stack.isEmpty() && (reverseDirection ? i >= startIndex : i < endIndex)) {
                slot = this.slots.get(i);
                itemStack = slot.getItem();
                if (!itemStack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= stack.getMaxStackSize()) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.setChanged();
                        bl = true;
                    } else if (itemStack.getCount() < stack.getMaxStackSize()) {
                        stack.shrink(stack.getMaxStackSize() - itemStack.getCount());
                        itemStack.setCount(stack.getMaxStackSize());
                        slot.setChanged();
                        bl = true;
                    }
                }
                if (reverseDirection) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        if (!stack.isEmpty()) {
            i = reverseDirection ? endIndex - 1 : startIndex;
            while (reverseDirection ? i >= startIndex : i < endIndex) {
                slot = this.slots.get(i);
                itemStack = slot.getItem();
                if (itemStack.isEmpty() && slot.mayPlace(stack)) {
                    if (stack.getCount() > slot.getMaxStackSize()) {
                        slot.setByPlayer(stack.split(slot.getMaxStackSize()));
                    } else {
                        slot.setByPlayer(stack.split(stack.getCount()));
                    }
                    slot.setChanged();
                    bl = true;
                    break;
                }
                if (reverseDirection) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        return bl;
    }

    private static class CardWritingContainer implements Container {
        private final Consumer<CardWritingContainer> slotsChangedCallback;
        private final UUID uuid;

        @NotNull
        protected final List<ItemStack> stacks = new ArrayList<>();

        public CardWritingContainer(Consumer<CardWritingContainer> slotsChangedCallback, UUID uuid) {
            this.slotsChangedCallback = slotsChangedCallback;
            this.uuid = uuid;
            stacks.add(ItemStack.EMPTY);
        }

        @Override
        public int getContainerSize() {
            return 1;
        }

        protected ItemStack getStack() {
            return stacks.get(0);
        }

        @Override
        public boolean isEmpty() {
            return getStack().isEmpty();
        }

        @Override
        public @NotNull ItemStack getItem(int slot) {
            return getStack();
        }

        @Override
        public @NotNull ItemStack removeItem(int slot, int amount) {
            ItemStack stack = ContainerHelper.removeItem(this.stacks, 0, amount);
            if (!stack.isEmpty()) {
                this.slotsChangedCallback.accept(this);
            }
            return stack;
        }

        @Override
        public @NotNull ItemStack removeItemNoUpdate(int slot) {
            return ContainerHelper.takeItem(this.stacks, 0);
        }

        @Override
        public void setItem(int slot, @NotNull ItemStack stack) {
            this.stacks.set(0, stack);
            if (!CardItem.isBound(stack) && NumismaticsTags.AllItemTags.CARDS.matches(stack))
                CardItem.set(stack, uuid);
            this.slotsChangedCallback.accept(this);
        }

        @Override
        public void setChanged() {

        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            this.stacks.set(0, ItemStack.EMPTY);
        }
    }
}
