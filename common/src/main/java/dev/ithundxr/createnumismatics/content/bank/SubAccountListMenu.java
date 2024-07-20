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

package dev.ithundxr.createnumismatics.content.bank;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.ithundxr.createnumismatics.base.item.DynamicContainer;
import dev.ithundxr.createnumismatics.base.item.SingleCallbackContainer;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.ItemWritingContainer;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.AuthorizationType;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.SubAccount;
import dev.ithundxr.createnumismatics.multiloader.PlayerSelection;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class SubAccountListMenu extends MenuBase<BankAccount> {
    public static final int TRUST_SLOTS = 27;
    public static final int TRUST_START_INDEX = 0;
    public static final int TRUST_END_INDEX = TRUST_START_INDEX + TRUST_SLOTS; // exclusive
    public static final int CARD_WRITING_INDEX = TRUST_END_INDEX;
    public static final int PLAYER_INV_START_INDEX = CARD_WRITING_INDEX + 1;
    public static final int PLAYER_HOTBAR_END_INDEX = PLAYER_INV_START_INDEX + 9;
    public static final int PLAYER_INV_END_INDEX = PLAYER_INV_START_INDEX + 36;

    private AuthorizedCardWritingContainer authorizedCardContainer;

    @Nullable
    private OpenSubAccountInformation openSubAccount;

    private DynamicContainer trustListContainer;

    public SubAccountListMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public SubAccountListMenu(MenuType<?> type, int id, Inventory inv, BankAccount contentHolder) {
        super(type, id, inv, contentHolder);
    }

    public boolean slotsActive() {
        return openSubAccount != null;
    }

    @Nullable OpenSubAccountInformation getOpenSubAccount() {
        return openSubAccount;
    }

    @Override
    protected BankAccount createOnClient(FriendlyByteBuf extraData) {
        return BankAccount.clientSideSubAccountList(extraData);
    }

    @Override
    protected void initAndReadInventory(BankAccount contentHolder) {}

    @Override
    protected void addSlots() {
        if (authorizedCardContainer == null)
            authorizedCardContainer = new AuthorizedCardWritingContainer();
        if (trustListContainer == null)
            trustListContainer = new DynamicContainer(() -> openSubAccount == null ? null : openSubAccount.container);

        int x = 33;
        int y = 20;

        for (int i = 0; i < TRUST_SLOTS; i++) {
            if (i % 9 == 0 && i > 0) {
                x = 33;
                y += 18;
            }
            addSlot(new InactiveBoundIDCardSlot(trustListContainer, i, x, y));
            x += 18;
        }

        addSlot(new InactiveUnboundAuthorizedCardSlot(authorizedCardContainer, CARD_WRITING_INDEX, 176, 85));

        addPlayerSlots(40-4, 140);
    }

    @Override
    protected void addPlayerSlots(int x, int y) {
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot)
            this.addSlot(new InactiveSlot(playerInventory, hotbarSlot, x + hotbarSlot * 18, y + 58));
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new InactiveSlot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18));
    }

    @Override
    protected void saveData(BankAccount contentHolder) {}

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (playerIn instanceof ServerPlayer) {
            clearContainer(player, authorizedCardContainer);
        }
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
        if (stack.isStackable() && startIndex >= TRUST_SLOTS) { // CHANGED LINE
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

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) { // index is slot that was clicked
        Slot clickedSlot = this.slots.get(index);

        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack slotStack = clickedSlot.getItem();

        if (!slotsActive())
            return slotStack;

        ItemStack returnStack = slotStack.copy();

        if (index <= CARD_WRITING_INDEX && !moveItemStackTo(slotStack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, false)) {
            return ItemStack.EMPTY; // failed to move to player inventory
        } else if (slotStack.getItem() instanceof IDCardItem && IDCardItem.isBound(slotStack) && !moveItemStackTo(slotStack, TRUST_START_INDEX, TRUST_END_INDEX, false)) {
            return ItemStack.EMPTY; // failed to move to id card slots
        } else if (slotStack.getItem() instanceof AuthorizedCardItem && !AuthorizedCardItem.isBound(slotStack) && !moveItemStackTo(slotStack, CARD_WRITING_INDEX, CARD_WRITING_INDEX + 1, false)) {
            return ItemStack.EMPTY; // failed to move to authorized card slot
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

    public void openSubAccountEditScreen(@Nullable UUID subAccountID) {
        SubAccount subAccount = contentHolder.getSubAccountNoAuth(subAccountID);
        if (subAccount != null) {
            openSubAccount = new OpenSubAccountInformation(subAccount);
        } else {
            openSubAccount = null;
        }

        if (player instanceof ServerPlayer) {
            slotsChanged(trustListContainer);
        }
    }

    private void sendUpdateToOthers() {
        sendUpdate(false);
    }

    private void sendUpdate(boolean includeSelf) {
        if (player instanceof ServerPlayer serverPlayer) {
            NumismaticsPackets.PACKETS.sendTo(PlayerSelection.allWith((p) -> {
                if (!includeSelf && p == serverPlayer)
                    return false;

                if (p.containerMenu instanceof SubAccountListMenu subAccountListMenu)
                    return subAccountListMenu.contentHolder.id.equals(contentHolder.id);

                return false;
            }), new UpdateSubAccountsPacket(contentHolder));
        }
    }

    public void addSubAccount(@NotNull String label) {
        if (player instanceof ServerPlayer) {
            contentHolder.addSubAccount(label);
            sendUpdate(true);
        } else {
            NumismaticsPackets.PACKETS.send(new AddSubAccountPacket(label));
        }
    }

    /** Must not be called with open SubAccount */
    public void removeSubAccount(@NotNull UUID subAccountID) {
        if (contentHolder == null)
            return;
        contentHolder.removeSubAccount(subAccountID);

        if (player instanceof ServerPlayer) {
            sendUpdateToOthers();
        } else {
            NumismaticsPackets.PACKETS.send(new RemoveSubAccountPacket(subAccountID));
        }
    }

    public void resetSubAccountSpending(@NotNull UUID subAccountID) {
        SubAccount subAccount = contentHolder.getSubAccountNoAuth(subAccountID);

        if (subAccount != null) {
            subAccount.getTotalLimit().resetSpent();
            subAccount.markDirty();
        }

        if (player instanceof ServerPlayer) {
            sendUpdateToOthers();
        } else {
            NumismaticsPackets.PACKETS.send(new ResetSubAccountSpendingPacket(subAccountID));
        }
    }

    public void setLimit(@NotNull UUID subAccountID, @Nullable Integer limit) {
        SubAccount subAccount = contentHolder.getSubAccountNoAuth(subAccountID);

        if (subAccount == null || !Objects.equals(subAccount.getTotalLimit().getLimit(), limit)) {
            if (subAccount != null)
                subAccount.getTotalLimit().setLimit(limit, false);

            if (player instanceof ServerPlayer) {
                sendUpdateToOthers();
            } else {
                NumismaticsPackets.PACKETS.send(new ConfigureSubAccountPacket(subAccountID, limit));
            }
        }
    }

    public void setAuthorizationType(@NotNull UUID subAccountID, @NotNull AuthorizationType authorizationType) {
        SubAccount subAccount = contentHolder.getSubAccountNoAuth(subAccountID);

        if (subAccount == null || subAccount.getAuthorizationType() != authorizationType) {
            if (subAccount != null)
                subAccount.setAuthorizationType(authorizationType);

            if (player instanceof ServerPlayer) {
                sendUpdateToOthers();
            } else {
                NumismaticsPackets.PACKETS.send(new ConfigureSubAccountPacket(subAccountID, authorizationType));
            }
        }
    }

    public void setLabel(@NotNull UUID subAccountID, @NotNull String label) {
        if (label.isBlank()) {
            label = "Sub Account";
        }

        SubAccount subAccount = contentHolder.getSubAccountNoAuth(subAccountID);

        if (subAccount == null || !subAccount.getLabel().equals(label)) {
            if (subAccount != null)
                subAccount.setLabel(label);

            if (player instanceof ServerPlayer) {
                sendUpdateToOthers();
            } else {
                NumismaticsPackets.PACKETS.send(new ConfigureSubAccountPacket(subAccountID, label));
            }
        }
    }

    private class InactiveUnboundAuthorizedCardSlot extends AuthorizedCardSlot.UnboundAuthorizedCardSlot {

        public InactiveUnboundAuthorizedCardSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && slotsActive();
        }
    }

    private class InactiveBoundIDCardSlot extends IDCardSlot.BoundIDCardSlot {

        public InactiveBoundIDCardSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return super.isActive() && slotsActive();
        }
    }

    private class InactiveSlot extends Slot {

        public InactiveSlot(Container pContainer, int pIndex, int pX, int pY) {
            super(pContainer, pIndex, pX, pY);
        }

        @Override
        public boolean isActive() {
            return slotsActive();
        }

    }

    class OpenSubAccountInformation {
        public final UUID authorizationID;
        public final Container container;

        public OpenSubAccountInformation(SubAccount subAccount) {
            this.authorizationID = subAccount.getAuthorizationID();
            this.container = new SingleCallbackContainer(subAccount.getTrustListContainer(), this::trustListChanged);
        }

        private void trustListChanged(Container container) {
            SubAccountListMenu.this.slotsChanged(container);
            SubAccount subAccount = contentHolder.getSubAccountNoAuth(authorizationID);
            if (subAccount != null)
                subAccount.markDirty();
        }
    }

    private class AuthorizedCardWritingContainer extends ItemWritingContainer<AuthorizedCardWritingContainer> {

        public AuthorizedCardWritingContainer() {
            super(SubAccountListMenu.this::slotsChanged);
        }

        @Override
        protected void doWriteItem(ItemStack stack) {
            if (NumismaticsTags.AllItemTags.AUTHORIZED_CARDS.matches(stack) && !AuthorizedCardItem.isBound(stack) && openSubAccount != null) {
                AuthorizedCardItem.set(stack, new AuthorizedCardItem.AuthorizationPair(contentHolder.id, openSubAccount.authorizationID));
            }
        }
    }
}
