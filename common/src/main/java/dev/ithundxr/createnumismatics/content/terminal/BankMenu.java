package dev.ithundxr.createnumismatics.content.terminal;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.SlotInputMergingCoinBag;
import dev.ithundxr.createnumismatics.content.coins.SlotOutputMergingCoinBag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BankMenu extends MenuBase<BankAccount> {
    public static final int COIN_SLOTS = Coin.values().length;
    protected ContainerData dataAccess;
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
        int x = 13;
        int y = 71;

        for (Coin coin : Coin.values()) {
            addSlot(new SlotOutputMergingCoinBag(contentHolder.linkedCoinBag, coin, x, y));
            x += 18;
        }

        addSlot(new SlotInputMergingCoinBag(contentHolder.linkedCoinBag, null, 159, y));

        addPlayerSlots(40, 152);
    }

    @Override
    protected void saveData(BankAccount contentHolder) {}

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        /*if (!contentHolder.isClientSide()) {
            balance.set(contentHolder.getBalance());
            broadcastChanges();
        }*/
        return ItemStack.EMPTY;
    }
}
