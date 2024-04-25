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

public class SlotOutputMergingCoinBag extends Slot {
    private static final Container emptyInventory = new SimpleContainer(0);
    private final MergingCoinBag coinBag;
    private final Coin coin;

    public SlotOutputMergingCoinBag(MergingCoinBag coinBag, Coin coin, int x, int y) {
        super(emptyInventory, 0, x, y);
        this.coinBag = coinBag;
        this.coin = coin;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack getItem() {
        return coinBag.asVisualStack(coin);
    }

    @Override
    public void set(@NotNull ItemStack stack) {}

    @Override
    public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {}

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return getMaxStackSize();
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return coinBag.get(coin).getFirst() > 0;
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        remove(numItemsCrafted);
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        int available = coinBag.get(coin).getFirst();
        amount = Math.min(amount, available);

        if (amount <= 0)
            return ItemStack.EMPTY;

        coinBag.subtract(coin, amount);
        return coin.asStack(amount);
    }

    @Override
    public @NotNull Optional<ItemStack> tryRemove(int count, int decrement, Player player) {
        decrement = Math.min(64, decrement);
        if (!this.mayPickup(player)) {
            return Optional.empty();
        }
        /*if (!this.allowModification(player) && decrement < this.getItem().getCount()) {
            return Optional.empty();
        }*/
        ItemStack itemStack = this.remove(count = Math.min(count, decrement));
        if (itemStack.isEmpty()) {
            return Optional.empty();
        }
        if (this.getItem().isEmpty()) {
            this.set(ItemStack.EMPTY);
        }
        return Optional.of(itemStack);
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, Numismatics.asResource("item/coin/outline/"+coin.getName()));
    }
}
