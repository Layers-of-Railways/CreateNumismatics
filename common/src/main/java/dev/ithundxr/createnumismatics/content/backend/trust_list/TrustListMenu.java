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

package dev.ithundxr.createnumismatics.content.backend.trust_list;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.MenuBase;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
import dev.ithundxr.createnumismatics.content.bank.IDCardSlot.BoundIDCardSlot;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.content.depositor.ProtectedScrollOptionBehaviour;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class TrustListMenu extends MenuBase<TrustListHolder> {
    public static final int CARD_SLOTS = 27;
    public static final int PLAYER_INV_START_INDEX = CARD_SLOTS;
    public static final int PLAYER_HOTBAR_END_INDEX = PLAYER_INV_START_INDEX + 9;
    public static final int PLAYER_INV_END_INDEX = PLAYER_INV_START_INDEX + 36;

    ItemStack renderedItem;

    public TrustListMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    protected TrustListMenu(MenuType<?> type, int id, Inventory inv, TrustListHolder contentHolder, ItemStack renderedItem) {
        super(type, id, inv, contentHolder);
        this.renderedItem = renderedItem;
    }

    public static MenuProvider provider(TrustListHolder contentHolder, ItemStack renderedItem) {
        return new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return Components.translatable("gui.numismatics.trust_list");
            }

            @Override
            public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                return new TrustListMenu(NumismaticsMenuTypes.TRUST_LIST.get(), i, inventory, contentHolder, renderedItem);
            }
        };
    }

    @Override
    protected TrustListHolder createOnClient(FriendlyByteBuf extraData) {
        renderedItem = extraData.readItem();
        ClientLevel world = Minecraft.getInstance().level;
        BlockEntity blockEntity = world.getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof SyncedBlockEntity syncedBE && syncedBE instanceof TrustListHolder trustListHolder) {
            syncedBE.readClient(extraData.readNbt());
            return trustListHolder;
        }
        return null;
    }

    @Override
    protected void initAndReadInventory(TrustListHolder contentHolder) {

    }

    @Override
    protected void addSlots() {
        int x = 16;
        int y = 21;

        for (int i = 0; i < CARD_SLOTS; i++) {
            if (i % 9 == 0 && i > 0) {
                x = 16;
                y += 18;
            }
            addSlot(new BoundIDCardSlot(contentHolder.getTrustListBackingContainer(), i, x, y));
            x += 18;
        }

        addPlayerSlots(40, 130);
    }

    @Override
    protected void saveData(TrustListHolder contentHolder) {

    }

    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot clickedSlot = this.slots.get(index);

        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack slotStack = CoinItem.clearDisplayedCount(clickedSlot.getItem());
        ItemStack returnStack = slotStack.copy();

        if (index < CARD_SLOTS) {
            int count = slotStack.getCount();
            if (!moveItemStackTo(slotStack, PLAYER_INV_START_INDEX, PLAYER_INV_END_INDEX, false))
                return ItemStack.EMPTY;

            returnStack = ItemStack.EMPTY;
            clickedSlot.remove(count);
        } else if (slotStack.getItem() instanceof IDCardItem && IDCardItem.isBound(slotStack) && !moveItemStackTo(slotStack, 0, CARD_SLOTS, false)) {
            return ItemStack.EMPTY; // failed to move to card slots
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
        if (stack.isStackable() && startIndex >= CARD_SLOTS) {
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

    public enum TrustListSham implements INamedIconOptions {
        NONE;

        @Override
        public AllIcons getIcon() {
            return AllIcons.I_VIEW_SCHEDULE;
        }

        @Override
        public String getTranslationKey() {
            return "numismatics.trust_list.configure";
        }
    }

    public static <BE extends SmartBlockEntity & MenuProvider & Trusted & TrustListHolder> ScrollOptionBehaviour<TrustListSham> makeConfigureButton(BE be, ValueBoxTransform slot, ItemStack displayStack) {
        return new ProtectedScrollOptionBehaviour<>(TrustListSham.class, Components.translatable("numismatics.trust_list.configure"), be,
            slot, be::isTrusted) {
            @Override
            public void onShortInteract(Player player, InteractionHand hand, Direction side) {
                if (be.isTrusted(player) && player instanceof ServerPlayer serverPlayer) {
                    Utils.openScreen(serverPlayer,
                        TrustListMenu.provider(be, displayStack),
                        (buf) -> {
                            buf.writeItem(displayStack);
                            be.sendToMenu(buf);
                        });
                } else {
                    super.onShortInteract(player, hand, side);
                }
            }

            @Override
            public boolean acceptsValueSettings() {
                return false;
            }
        };
    }

    public static <BE extends SmartBlockEntity & MenuProvider & Trusted & TrustListHolder> void openMenu(BE be, ServerPlayer player, ItemStack displayStack) {
        if (be.isTrusted(player)) {
            Utils.openScreen(player,
                TrustListMenu.provider(be, displayStack),
                (buf) -> {
                    buf.writeItem(displayStack);
                    be.sendToMenu(buf);
                });
        }
    }
}
