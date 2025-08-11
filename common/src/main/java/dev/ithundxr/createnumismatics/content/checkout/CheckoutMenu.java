package dev.ithundxr.createnumismatics.content.checkout;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.bank.CardSlot;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class CheckoutMenu extends MenuBase<DeferredCheckoutOrderMenuProvider> {
    private CheckoutMenu.CardSwitchContainer cardSwitchContainer;
    protected UUID currentCardUUID = Utils.emptyUUID;

    public CheckoutMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public CheckoutMenu(MenuType<?> type, int id, Inventory inv, DeferredCheckoutOrderMenuProvider contentHolder) {
        super(type, id, inv, contentHolder);
    }

    @Override
    protected DeferredCheckoutOrderMenuProvider createOnClient(RegistryFriendlyByteBuf extraData) {
        return DeferredCheckoutOrderMenuProvider.clientSide(extraData);
    }

    @Override
    protected void initAndReadInventory(DeferredCheckoutOrderMenuProvider contentHolder) {
    }

    @Override
    protected void addSlots() {
        if (cardSwitchContainer == null)
            cardSwitchContainer = new CheckoutMenu.CardSwitchContainer(this::slotsChanged, (id) -> {
                currentCardUUID = id;
                return true;
            });

        addSlot(new CardSlot.BoundCardSlot(cardSwitchContainer, 0, 148, 73));
        addPlayerSlots(40, 152);
    }

    @Override
    protected void addPlayerSlots(int x, int y) {
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
            this.addSlot(new LockableSlot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58, hotbarSlot == playerInventory.selected));
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int slot = col + row * 9 + 9;
                this.addSlot(new LockableSlot(playerInventory, slot, x + col * 18, y + row * 18, slot == playerInventory.selected));
            }
        }
    }

    @Override
    protected void saveData(DeferredCheckoutOrderMenuProvider contentHolder) {
    }

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

        if (NumismaticsTags.AllItemTags.CARDS.matches(clickedSlot.getItem())) {
            if (index == 0) // They've clicked the card in the slot
                moveItemStackTo(clickedSlot.getItem(), 1, player.getInventory().getContainerSize() + 1, false);
            else // They've clicked a card in their inventory
                moveItemStackTo(clickedSlot.getItem(), 0, 1, false);
        }

        return ItemStack.EMPTY;
    }

    private class CardSwitchContainer implements Container {
        private final Consumer<CheckoutMenu.CardSwitchContainer> slotsChangedCallback;
        private final Function<UUID, Boolean> uuidChangedCallback; // should return success

        @NotNull
        protected final List<ItemStack> stacks = new ArrayList<>();

        public CardSwitchContainer(Consumer<CheckoutMenu.CardSwitchContainer> slotsChangedCallback, Function<UUID, Boolean> uuidChangedCallback) {
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
                    CheckoutMenu.this.clearContainer(CheckoutMenu.this.player, this);
                }
            }
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
