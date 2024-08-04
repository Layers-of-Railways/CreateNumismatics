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

package dev.ithundxr.createnumismatics.content.salepoint;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.content.backend.IDeductable;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.bank.AnyCardSlot;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.registry.packets.SalepointCardPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SalepointPurchaseMenu extends MenuBase<SalepointBlockEntity> {
    public static final int CARD_SLOT_INDEX = 0;
    public static final int PLAYER_INV_START_INDEX = CARD_SLOT_INDEX + 1;
    public static final int PLAYER_HOTBAR_END_INDEX = PLAYER_INV_START_INDEX + 9; // exclusive
    public static final int PLAYER_INV_END_INDEX = PLAYER_INV_START_INDEX + 36; // exclusive
    public static final int DISPLAY_SLOT_INDEX = PLAYER_INV_END_INDEX; // may not always actually exist

    @ApiStatus.Internal
    public @Nullable Component serverSentCardMessage;

    @ApiStatus.Internal
    public int serverSentMaxWithdrawal;

    @ApiStatus.Internal
    public @Nullable Component serverSentStateMessage;

    private Container purchaseCardContainer;

    public SalepointPurchaseMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public SalepointPurchaseMenu(MenuType<?> type, int id, Inventory inv, SalepointBlockEntity contentHolder) {
        super(type, id, inv, contentHolder);
    }

    @Override
    protected SalepointBlockEntity createOnClient(FriendlyByteBuf extraData) {
        ClientLevel world = Minecraft.getInstance().level;
        BlockEntity blockEntity = world.getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof SalepointBlockEntity salepointBE) {
            salepointBE.readClient(extraData.readNbt());
            return salepointBE;
        }
        return null;
    }

    @Override
    protected void initAndReadInventory(SalepointBlockEntity contentHolder) {}

    @Override
    protected void addSlots() {
        if (purchaseCardContainer == null) {
            purchaseCardContainer = new SimpleContainer(1) {
                @Override
                public void setChanged() {
                    super.setChanged();

                    if (!(player instanceof ServerPlayer serverPlayer))
                        return;

                    ItemStack card = getItem(0);

                    serverSentCardMessage = null;
                    serverSentMaxWithdrawal = -1;

                    if (!card.isEmpty()) {
                        ReasonHolder reasonHolder = new ReasonHolder();
                        IDeductable deductable = IDeductable.get(card, player, reasonHolder);

                        if (deductable == null) {
                            serverSentCardMessage = reasonHolder.getMessageOrDefault(Components.translatable("gui.numismatics.salepoint.invalid_card"));
                        } else {
                            serverSentMaxWithdrawal = deductable.getMaxWithdrawal();
                        }
                    }

                    NumismaticsPackets.PACKETS.sendTo(serverPlayer, new SalepointCardPacket(serverSentCardMessage, serverSentMaxWithdrawal, serverSentStateMessage));
                }
            };
        }

        addSlot(new AnyCardSlot.BoundAnyCardSlot(purchaseCardContainer, 0, 10, 114));

        addPlayerSlots(53, 157);

        ISalepointState<?> salepointState = getSalepointState();
        if (salepointState != null && salepointState.purchaseGuiHasDisplaySlot()) {
            //noinspection DataFlowIssue
            addSlot(salepointState.createPurchaseGuiDisplaySlot(contentHolder.getLevel(), contentHolder.getBlockPos(), player));
        }
    }

    @Override
    protected void saveData(SalepointBlockEntity contentHolder) {}

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (playerIn instanceof ServerPlayer) {
            clearContainer(player, purchaseCardContainer);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;
        ItemStack stack = clickedSlot.getItem();

        if (index == CARD_SLOT_INDEX) { // removing card
            if (!moveItemStackTo(stack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, false))
                return ItemStack.EMPTY;
        } else {
            /*
            priority:
            1. Card slot
            2. Player inventory
             */
            if ((NumismaticsTags.AllItemTags.CARDS.matches(stack) || NumismaticsTags.AllItemTags.AUTHORIZED_CARDS.matches(stack)) && !moveItemStackTo(stack, CARD_SLOT_INDEX, CARD_SLOT_INDEX+1, false)) {
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
    public boolean stillValid(Player player) {
        tick();
        return super.stillValid(player);
    }

    private int lazyTickCounter = 0;

    private void tick() {
        lazyTickCounter++;

        if (lazyTickCounter >= 10) {
            lazyTickCounter = 0;
            updateStateMessage();
        }
    }

    protected @Nullable ISalepointState<?> getSalepointState() {
        if (contentHolder.salepointState == null)
            return null;
        return contentHolder.salepointState.state();
    }

    public ItemStack getCard() {
        return purchaseCardContainer.getItem(0);
    }

    @SuppressWarnings("DataFlowIssue")
    private void updateStateMessage() {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        serverSentStateMessage = null;

        ISalepointState<?> salepointState = getSalepointState();
        if (salepointState == null) {
            serverSentStateMessage = Components.translatable("gui.numismatics.salepoint.no_state");
        } else {
            ReasonHolder reasonHolder = new ReasonHolder();
            if (!salepointState.isValidForPurchase(contentHolder.getLevel(), contentHolder.getTargetedPos(), reasonHolder)) {
                serverSentStateMessage = reasonHolder.getMessageOrDefault(Components.translatable("gui.numismatics.salepoint.invalid_state"));
            }
        }

        NumismaticsPackets.PACKETS.sendTo(serverPlayer, new SalepointCardPacket(serverSentCardMessage, serverSentMaxWithdrawal, serverSentStateMessage));
    }
}
