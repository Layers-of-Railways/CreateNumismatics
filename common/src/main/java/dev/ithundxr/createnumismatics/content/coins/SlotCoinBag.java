package dev.ithundxr.createnumismatics.content.coins;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class SlotCoinBag extends Slot {

    private static final Container emptyInventory = new SimpleContainer(0);
    private final CoinBag coinBag;
    private final Coin coin;
    private final boolean canInsert;
    private final boolean canExtract;

    public SlotCoinBag(CoinBag coinBag, Coin coin, int x, int y, boolean canInsert, boolean canExtract) {
        super(emptyInventory, 0, x, y);
        this.coinBag = coinBag;
        this.coin = coin;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!canInsert)
            return false;

        if (stack.isEmpty())
            return false;

        return stack.getItem() instanceof CoinItem;
    }

    @Override
    public @NotNull ItemStack getItem() {
        return coinBag.asStack(coin);
    }

    @Override
    public void set(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof CoinItem coinItem && coinItem.coin == coin) {
            coinBag.set(coin, stack.getCount());
            setChanged();
        }
    }

    @Override
    public void initialize(@NotNull ItemStack stack) {
        set(stack);
    }

    @Override
    public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {}

    @Override
    public int getMaxStackSize() {
        return NumismaticsItems.getCoin(coin).get().getMaxStackSize();
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        if (!canExtract)
            return false;

        return coinBag.get(coin) > 0;
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        amount = Math.min(amount, coinBag.get(coin));

        if (amount <= 0)
            return ItemStack.EMPTY;

        coinBag.set(coin, coinBag.get(coin) - amount);
        return NumismaticsItems.getCoin(coin).asStack(amount);
    }
}
