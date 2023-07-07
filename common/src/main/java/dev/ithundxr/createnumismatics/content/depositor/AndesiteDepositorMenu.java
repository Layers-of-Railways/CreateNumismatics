package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.content.coins.SlotCoinBag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
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
        int x = 19;
        int y = 21;

        for (Coin coin : Coin.values()) {
            addSlot(new SlotCoinBag(contentHolder.inventory, coin, x, y, false, true));
            x += 18;
        }

        addPlayerSlots(31, 64);
    }

    @Override
    protected void saveData(AndesiteDepositorBlockEntity contentHolder) {}

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (clickType == ClickType.THROW)
            return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot clickedSlot = getSlot(index);
        if (!clickedSlot.hasItem())
            return ItemStack.EMPTY;
        ItemStack stack = clickedSlot.getItem();

        Coin coin = ((CoinItem) stack.getItem()).coin;
        int startCount = stack.getCount();

        if (index < COIN_SLOTS) {
            moveItemStackTo(stack, COIN_SLOTS, slots.size(), false);
        } else {
            return ItemStack.EMPTY;
        }

        int diff = startCount - stack.getCount();

        if (diff > 0) {
            contentHolder.inventory.subtract(coin, diff);
            clickedSlot.setChanged();
        } else if (diff < 0) {
            contentHolder.inventory.add(coin, -diff);
            clickedSlot.setChanged();
        }

        return ItemStack.EMPTY;
    }
}
