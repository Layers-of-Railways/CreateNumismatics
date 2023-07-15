package dev.ithundxr.createnumismatics.content.coins;

import com.mojang.datafixers.util.Pair;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SlotInputMergingCoinBag extends Slot {
    private static final Container emptyInventory = new SimpleContainer(0);
    private final MergingCoinBag coinBag;

    @Nullable
    private final Coin coin;

    public SlotInputMergingCoinBag(MergingCoinBag coinBag, @Nullable Coin coin, int x, int y) {
        super(emptyInventory, 0, x, y);
        this.coinBag = coinBag;
        this.coin = coin;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        return stack.getItem() instanceof CoinItem coinItem && (coin == null || coinItem.coin == coin);
    }

    @Override
    public @NotNull ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void set(ItemStack stack) {
        if (stack.getItem() instanceof CoinItem coinItem && (coin == null || coinItem.coin == coin)) {
            coinBag.add(coinItem.coin, stack.getCount());
            stack.setCount(0);
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
        return Integer.MAX_VALUE;//NumismaticsItems.getCoin(coin).get().getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return getMaxStackSize();
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return false;
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull Optional<ItemStack> tryRemove(int count, int decrement, @NotNull Player player) {
        return Optional.empty();
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, Numismatics.asResource(coin == null ? "item/coin/outline/animated" : "item/coin/outline/"+coin.getName()));
    }
}
