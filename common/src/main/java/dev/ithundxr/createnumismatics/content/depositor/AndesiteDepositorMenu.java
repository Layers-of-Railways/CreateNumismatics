package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.bank.CardSlot;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.content.coins.SlotDiscreteCoinBag;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class AndesiteDepositorMenu extends MenuBase<AndesiteDepositorBlockEntity> {
    public static final int COIN_SLOTS = Coin.values().length;
    public static final int CARD_SLOT_INDEX = COIN_SLOTS;
    public static final int PLAYER_INV_START_INDEX = CARD_SLOT_INDEX + 1;
    public static final int PLAYER_HOTBAR_END_INDEX = PLAYER_INV_START_INDEX + 9;
    public static final int PLAYER_INV_END_INDEX = PLAYER_INV_START_INDEX + 36;
    public AndesiteDepositorMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public AndesiteDepositorMenu(MenuType<?> type, int id, Inventory inv, AndesiteDepositorBlockEntity contentHolder) {
        super(type, id, inv, contentHolder);
    }

    @Override
    protected AndesiteDepositorBlockEntity createOnClient(FriendlyByteBuf extraData) {
        ClientLevel world = Minecraft.getInstance().level;
        BlockEntity blockEntity = world.getBlockEntity(extraData.readBlockPos());
        if (blockEntity instanceof AndesiteDepositorBlockEntity andesiteDepositorBE) {
            andesiteDepositorBE.readClient(extraData.readNbt());
            return andesiteDepositorBE;
        }
        return null;
    }

    @Override
    protected void initAndReadInventory(AndesiteDepositorBlockEntity contentHolder) {}

    @Override
    protected void addSlots() {
        int x = 11;
        int y = 21;

        for (Coin coin : Coin.values()) {
            x += 18;
            addSlot(new SlotDiscreteCoinBag(contentHolder.inventory, coin, x, y, true, true));
        }
        addSlot(new CardSlot.BoundCardSlot(contentHolder.cardContainer, 0, 11, y)); // make here to preserve slot order

        addPlayerSlots(31, 64);
    }

    @Override
    protected void saveData(AndesiteDepositorBlockEntity contentHolder) {}

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        //if (clickType == ClickType.THROW)
        //    return;
        super.clicked(slotId, button, clickType, player);
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

            moveItemStackTo(stack, COIN_SLOTS, slots.size(), false);

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
        } else { // player inventory
            if (stack.getItem() instanceof CoinItem && !moveItemStackTo(stack, 0, COIN_SLOTS, false)) {
                return ItemStack.EMPTY;
            } else if (NumismaticsTags.AllItemTags.CARDS.matches(stack) && !moveItemStackTo(stack, CARD_SLOT_INDEX, CARD_SLOT_INDEX+1, false)) {
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
}
