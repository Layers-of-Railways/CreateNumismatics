package dev.ithundxr.createnumismatics.content.coins;

import com.simibubi.create.foundation.utility.Couple;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class MergingCoinBag implements CoinBag {

    private int value;

    public MergingCoinBag() {
        this(0);
    }

    public MergingCoinBag(int value) {
        setRaw(value);
    }

    @Override
    public void add(Coin coin, int count) {
        setRaw(getValue() + coin.toSpurs(count));
    }

    @Override
    public void subtract(Coin coin, int count) {
        int value = this.getValue() - coin.toSpurs(count);
        value = Math.max(0, value);
        setRaw(value);
    }

    @Override
    public void set(Coin coin, int count, int spurRemainder) {
        count = Math.max(0, count);
        setRaw(coin.toSpurs(count) + spurRemainder);
    }

    protected void setRaw(int value) {
        this.value = value;
    }

    @Override
    public Couple<Integer> get(Coin coin) {
        return coin.convert(getValue());
    }

    @Override
    public ItemStack asStack(Coin coin) {
        int amt = get(coin).getFirst();
        if (amt == 0)
            return ItemStack.EMPTY;

        return NumismaticsItems.getCoin(coin).asStack(amt);
    }

    public ItemStack asVisualStack(Coin coin) {
        int amt = get(coin).getFirst();
        if (amt == 0)
            return ItemStack.EMPTY;
        return CoinItem.setDisplayedCount(NumismaticsItems.getCoin(coin).asStack(Math.min(64, amt)), amt);
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putInt("CoinValue", getValue());
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        setRaw(nbt.getInt("CoinValue"));
    }

    @Override
    public void clear() {
        setRaw(0);
    }
}
