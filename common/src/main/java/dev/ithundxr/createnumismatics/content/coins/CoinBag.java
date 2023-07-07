package dev.ithundxr.createnumismatics.content.coins;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CoinBag {
    private final Map<Coin, Integer> coins = new HashMap<>();
    private int value = 0;

    protected CoinBag(Map<Coin, Integer> coins) {
        this.coins.putAll(coins);
        calculateValue();
    }

    public CoinBag() {}

    private void calculateValue() {
        for (Map.Entry<Coin, Integer> entry : coins.entrySet()) {
            this.value += entry.getKey().toSpurs(entry.getValue());
        }
    }

    public void add(Coin coin, int count) {
        this.coins.put(coin, get(coin) + count);
        calculateValue();
    }

    public void subtract(Coin coin, int count) {
        this.coins.put(coin, Math.max(0, get(coin) - count));
        calculateValue();
    }

    public void set(Coin coin, int count) {
        count = Math.max(0, count);
        this.coins.put(coin, count);
        calculateValue();
    }

    public int get(Coin coin) {
        return this.coins.getOrDefault(coin, 0);
    }

    public ItemStack asStack(Coin coin) {
        int amt = get(coin);
        if (amt == 0)
            return ItemStack.EMPTY;

        return NumismaticsItems.getCoin(coin).asStack(amt);
    }

    public int getValue() {
        return value;
    }

    public boolean isEmpty() {
        return value == 0;
    }

    public CompoundTag save(CompoundTag nbt) {
        for (Map.Entry<Coin, Integer> entry : coins.entrySet()) {
            if (entry.getValue() > 0) {
                nbt.putInt(entry.getKey().name(), entry.getValue());
            }
        }
        return nbt;
    }

    public void load(CompoundTag nbt) {
        coins.clear();
        for (Coin coin : Coin.values()) {
            if (nbt.contains(coin.name())) {
                coins.put(coin, nbt.getInt(coin.name()));
            }
        }
    }

    public static CoinBag of(CompoundTag nbt) {
        CoinBag bag = new CoinBag();
        bag.load(nbt);
        return bag;
    }

    public static CoinBag of(Map<Coin, Integer> coins) {
        return new CoinBag(coins);
    }

    public static CoinBag of() {
        return new CoinBag();
    }

    public void clear() {
        coins.clear();
    }
}
