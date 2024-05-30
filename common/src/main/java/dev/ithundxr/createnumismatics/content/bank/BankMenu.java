/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.content.bank;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.content.coins.SlotInputMergingCoinBag;
import dev.ithundxr.createnumismatics.content.coins.SlotOutputMergingCoinBag;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class BankMenu extends MenuBase<BankAccount> {
    public static final int COIN_SLOTS = Coin.values().length;
    public static final int COIN_INPUT_SLOT_INDEX = COIN_SLOTS;
    public static final int CARD_SLOT_INDEX = COIN_INPUT_SLOT_INDEX + 1;
    public static final int PLAYER_INV_START_INDEX = CARD_SLOT_INDEX + 1;
    public static final int PLAYER_HOTBAR_END_INDEX = PLAYER_INV_START_INDEX + 9;
    public static final int PLAYER_INV_END_INDEX = PLAYER_INV_START_INDEX + 36;
    protected ContainerData dataAccess;
    private CardSwitchContainer cardSwitchContainer;
    public BankMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public BankMenu(MenuType<?> type, int id, Inventory inv, BankAccount contentHolder, ContainerData dataAccess) {
        super(type, id, inv, contentHolder);
        this.dataAccess = dataAccess;
        addDataSlots(dataAccess);
    }

    @Override
    protected BankAccount createOnClient(FriendlyByteBuf extraData) {
        BankAccount account = BankAccount.clientSide(extraData);
        this.dataAccess = account.dataAccess;
        addDataSlots(dataAccess);
        return account;
    }

    @Override
    protected void initAndReadInventory(BankAccount contentHolder) {}

    @Override
    protected void addSlots() {
        if (cardSwitchContainer == null)
            cardSwitchContainer = new CardSwitchContainer(this::slotsChanged, this::switchTo);
        int x = 13;
        int y = 71;

        for (Coin coin : Coin.values()) {
            addSlot(new SlotOutputMergingCoinBag(contentHolder.linkedCoinBag, coin, x, y));
            x += 18;
        }

        addSlot(new SlotInputMergingCoinBag(contentHolder.linkedCoinBag, null, 159, y));

        addSlot(new CardSlot.BoundCardSlot(cardSwitchContainer, 0, 8, 109));

        addPlayerSlots(40, 152);
    }

    private boolean switchTo(UUID otherAccount) {
        if (player instanceof ServerPlayer serverPlayer) {
            BankAccount account = Numismatics.BANK.getAccount(otherAccount);
            if (account != null) {
                if (account.isAuthorized(serverPlayer)) {
                    Utils.openScreen(serverPlayer, account, account::sendToMenu);
                }
                return true;
            } else {
                return false;
            }
        } else {
            return true; // client
        }
    }

    @Override
    protected void saveData(BankAccount contentHolder) {}

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (playerIn instanceof ServerPlayer) {
            clearContainer(player, cardSwitchContainer);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) { // index is slot that was clicked
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
        } else if (slotStack.getItem() instanceof CoinItem && !moveItemStackTo(slotStack, COIN_INPUT_SLOT_INDEX, COIN_INPUT_SLOT_INDEX + 1, false)) {
            return ItemStack.EMPTY; // failed to move to coin input slot
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

    private class CardSwitchContainer implements Container {
        private final Consumer<CardSwitchContainer> slotsChangedCallback;
        private final Function<UUID, Boolean> uuidChangedCallback; // should return success

        @NotNull
        protected final List<ItemStack> stacks = new ArrayList<>();

        public CardSwitchContainer(Consumer<CardSwitchContainer> slotsChangedCallback, Function<UUID, Boolean> uuidChangedCallback) {
            this.slotsChangedCallback = slotsChangedCallback;
            this.uuidChangedCallback = uuidChangedCallback;
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
            if (CardItem.isBound(stack) && NumismaticsTags.AllItemTags.CARDS.matches(stack)) {
                if (!this.uuidChangedCallback.apply(CardItem.get(stack))) {
                    // Non-existent account
                    stacks.set(0, CardItem.clear(stack));
                    BankMenu.this.clearContainer(BankMenu.this.player, this);
                }
            }
            this.slotsChangedCallback.accept(this);
        }

        @Override
        public void setChanged() {}

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
