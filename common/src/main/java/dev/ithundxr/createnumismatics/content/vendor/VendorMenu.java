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

package dev.ithundxr.createnumismatics.content.vendor;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.ithundxr.createnumismatics.content.backend.BigStackSizeContainerSynchronizer;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.IGhostItemMenu;
import dev.ithundxr.createnumismatics.content.backend.IScrollableSlotMenu;
import dev.ithundxr.createnumismatics.content.bank.AnyCardSlot;
import dev.ithundxr.createnumismatics.content.coins.CoinDisplaySlot;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.content.coins.SlotDiscreteCoinBag;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class VendorMenu extends MenuBase<VendorBlockEntity> implements IScrollableSlotMenu, IGhostItemMenu {
    public static final int COIN_SLOTS = Coin.values().length;
    public static final int CARD_SLOT_INDEX = COIN_SLOTS;
    public static final int FILTER_SLOT_INDEX = CARD_SLOT_INDEX + 1;
    public static final int INV_START_INDEX = FILTER_SLOT_INDEX + 1;
    public static final int INV_END_INDEX = INV_START_INDEX + 9; // exclusive
    public static final int PLAYER_INV_START_INDEX = INV_END_INDEX;
    public static final int PLAYER_HOTBAR_END_INDEX = PLAYER_INV_START_INDEX + 9; // exclusive
    public static final int PLAYER_INV_END_INDEX = PLAYER_INV_START_INDEX + 36; // exclusive
    public VendorMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public VendorMenu(MenuType<?> type, int id, Inventory inv, VendorBlockEntity contentHolder) {
        super(type, id, inv, contentHolder);
    }

    @Override
    protected VendorBlockEntity createOnClient(FriendlyByteBuf extraData) {
        ClientLevel world = Minecraft.getInstance().level;
        BlockEntity blockEntity = world.getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof VendorBlockEntity vendorBE) {
            vendorBE.readClient(extraData.readNbt());
            return vendorBE;
        }
        return null;
    }

    @Override
    protected void initAndReadInventory(VendorBlockEntity contentHolder) {}

    @Override
    protected void addSlots() {
        int x = 14+16+4+18;
        int y = 122+6;

        for (Coin coin : Coin.values()) {
            addSlot(new SlotDiscreteCoinBag(contentHolder.inventory, coin, x, y, true, true));
            x += 18;
        }
        addSlot(new AnyCardSlot.BoundAnyCardSlot(contentHolder.cardContainer, 0, 170+4+18, y)); // make here to preserve slot order
        addSlot(new Slot(contentHolder.filterContainer, 0, 142+5+20, y));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                addSlot(new FilteringSlot(contentHolder, j + i * 3, 87+9 + j * 18, 49 + i * 18 + 11, contentHolder::matchesFilterItem));
            }
        }

        addPlayerSlots(67, 171);

        // label coins

        int labelX1 = 12+6;
        int labelX2 = labelX1 + 86 + 54 + 6;
        int labelY = 46;
        int labelYIncrement = 22;

        for (int i = 0; i < 6; i++) {
            Coin coin = Coin.values()[i];
            int slotX = i < 3 ? labelX1 : labelX2;
            int slotY = labelY + ((i%3) * labelYIncrement);

            addSlot(new CoinDisplaySlot(coin, slotX, slotY));
        }
    }

    @Override
    protected void saveData(VendorBlockEntity contentHolder) {}

    @Override
    public boolean canTakeItemForPickAll(@NotNull ItemStack stack, Slot slot) {
        return slot.container != contentHolder.filterContainer && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        //if (clickType == ClickType.THROW)
        //    return;
        ItemStack held = getCarried();
        Inventory inventory = player.getInventory();
        if (slotId == FILTER_SLOT_INDEX) {
            if (clickType == ClickType.CLONE || contentHolder.isFilterSlotLegacy()) {
                super.clicked(slotId, button, clickType, player);
                return;
            }

            ItemStack insert;
            if (clickType == ClickType.SWAP) {
                insert = inventory.getItem(button).copy();
            } else if (held.isEmpty()) {
                insert = ItemStack.EMPTY;
            } else {
                insert = held.copy();
            }
            getSlot(slotId).set(insert);
        } else {
            super.clicked(slotId, button, clickType, player);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;
        ItemStack stack = clickedSlot.getItem();

        if (index < COIN_SLOTS) { // extracting coins
            if (!(stack.getItem() instanceof CoinItem coinItem))
                return ItemStack.EMPTY;

            Coin coin = coinItem.coin;
            int startCount = stack.getCount();

            moveItemStackTo(stack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, false);

            int diff = startCount - stack.getCount();

            if (diff > 0) {
                contentHolder.inventory.subtract(coin, diff);
                clickedSlot.setChanged();
            } else if (diff < 0) {
                contentHolder.inventory.add(coin, -diff);
                clickedSlot.setChanged();
            }

            return ItemStack.EMPTY;
        } else if (index == CARD_SLOT_INDEX) { // removing card
            if (!moveItemStackTo(stack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, false))
                return ItemStack.EMPTY;
        } else if (index == FILTER_SLOT_INDEX) { // removing filter item
            if (contentHolder.isFilterSlotLegacy()) {
                if (!moveItemStackTo(stack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, false))
                    return ItemStack.EMPTY;
                else
                    contentHolder.filterContainer.setChanged();
            } else {
                clickedSlot.set(ItemStack.EMPTY);
            }
        } else if (INV_START_INDEX <= index && index < INV_END_INDEX) { // removing stock
            if (!moveItemStackTo(stack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, false))
                return ItemStack.EMPTY;
        } else { // player inventory
            /*
            priority:
            1. Coin slots
            2. Card slot
            3. Filter slot (if empty)
            4. Inventory (if accepted)
            5. Player inventory
             */
            if (stack.getItem() instanceof CoinItem && !moveItemStackTo(stack, 0, COIN_SLOTS, false)) {
                return ItemStack.EMPTY;
            } else if (NumismaticsTags.AllItemTags.CARDS.matches(stack) && !moveItemStackTo(stack, CARD_SLOT_INDEX, CARD_SLOT_INDEX+1, false)) {
                return ItemStack.EMPTY;
            } else if (contentHolder.filterContainer.isEmpty()) {
                // stack copied to prevent clearing the source (player inventory) slot
                moveItemStackTo(stack.copy(), FILTER_SLOT_INDEX, FILTER_SLOT_INDEX +1, false);
                return ItemStack.EMPTY;
            } else if (contentHolder.matchesFilterItem(stack) && !moveItemStackTo(stack, INV_START_INDEX, INV_END_INDEX, false)) {
                return ItemStack.EMPTY;
            } else if (index >= PLAYER_INV_START_INDEX && index < PLAYER_HOTBAR_END_INDEX && !moveItemStackTo(stack, PLAYER_HOTBAR_END_INDEX, PLAYER_INV_END_INDEX, false)) {
                return ItemStack.EMPTY;
            } else if (index >= PLAYER_HOTBAR_END_INDEX && index < PLAYER_INV_END_INDEX && !moveItemStackTo(stack, PLAYER_INV_START_INDEX, PLAYER_HOTBAR_END_INDEX, false)) {
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setSynchronizer(@NotNull ContainerSynchronizer synchronizer) {
        if (player instanceof ServerPlayer serverPlayer) {
            super.setSynchronizer(new BigStackSizeContainerSynchronizer(serverPlayer));
            return;
        }

        super.setSynchronizer(synchronizer);
    }

    @Override
    public void scrollSlot(int slotID, double delta, boolean shift) {
        if (isSlotGhost(slotID)) {
            Slot slot = getSlot(slotID);
            if (!slot.hasItem())
                return;

            ItemStack stack = slot.getItem();
            int count = stack.getCount();
            int offset = delta > 0 ? 1 : -1;
            if (shift)
                offset *= 4;
            count += offset;
            count = Math.max(0, Math.min(count, stack.getMaxStackSize()));
            slot.set(count == 0 ? ItemStack.EMPTY : stack.copyWithCount(count));
        }
    }

    @Override
    public void setGhostStackInSlot(int slotID, ItemStack stack) {
        if (isSlotGhost(slotID)) {
            getSlot(slotID).set(stack);
        }
    }

    @Override
    public boolean isSlotGhost(int slotID) {
        return slotID == FILTER_SLOT_INDEX && !contentHolder.isFilterSlotLegacy();
    }
}
