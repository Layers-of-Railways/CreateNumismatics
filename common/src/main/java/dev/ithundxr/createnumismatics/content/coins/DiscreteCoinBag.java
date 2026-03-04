package dev.ithundxr.createnumismatics.content.coins;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DiscreteCoinBag implements CoinBag {
    private final Map<Coin, Integer> coins = new HashMap<>();
    private int value = 0;

    protected DiscreteCoinBag(Map<Coin, Integer> coins) {
        this.coins.putAll(coins);
        calculateValue();
    }

    public DiscreteCoinBag() {}

    private void calculateValue() {
        this.value = 0;
        for (Map.Entry<Coin, Integer> entry : coins.entrySet()) {
            this.value += entry.getKey().toSpurs(entry.getValue());
        }
    }

    @Override
    public void add(Coin coin, int count) {
        this.coins.put(coin, get(coin).getFirst() + count);
        calculateValue();
    }

    @Override
    public void subtract(Coin coin, int count) {
        this.coins.put(coin, Math.max(0, get(coin).getFirst() - count));
        calculateValue();
    }

    @Override
    public void set(Coin coin, int count, int spurRemainder) {
        if (spurRemainder != 0) {
            Numismatics.LOGGER.warn("DiscreteCoinBag.set() called with spurRemainder != 0");
        }
        count = Math.max(0, count);
        this.coins.put(coin, count);
        calculateValue();
    }

    public void setDiscrete(Coin coin, int count) {
        set(coin, count, 0);
    }

    @Override
    public Couple<Integer> get(Coin coin) {
        return Couple.create(this.coins.getOrDefault(coin, 0), 0);
    }

    public int getDiscrete(Coin coin) {
        return get(coin).getFirst();
    }

    @Override
    public ItemStack asStack(Coin coin) {
        int amt = get(coin).getFirst();
        if (amt == 0)
            return ItemStack.EMPTY;

        return NumismaticsItems.getCoin(coin).asStack(amt);
    }

    public Map<Coin, Integer> asMap() { return new HashMap<>(this.coins); }

    @Override
    public int getValue() {
        return value;
    }

    public boolean isEmpty() {
        return value == 0;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        for (Map.Entry<Coin, Integer> entry : coins.entrySet()) {
            if (entry.getValue() > 0) {
                tag.putInt(entry.getKey().name(), entry.getValue());
            }
        }
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        coins.clear();
        for (Coin coin : Coin.values()) {
            if (tag.contains(coin.name())) {
                coins.put(coin, tag.getInt(coin.name()));
            }
        }
        calculateValue();
    }

    public static DiscreteCoinBag of(CompoundTag tag) {
        DiscreteCoinBag bag = new DiscreteCoinBag();
        bag.load(tag);
        return bag;
    }

    public static DiscreteCoinBag of(Map<Coin, Integer> coins) {
        return new DiscreteCoinBag(coins);
    }

    public static DiscreteCoinBag ofGreedy(int totalSpurValue) {
        DiscreteCoinBag bag = new DiscreteCoinBag();
        int spurs = totalSpurValue;
        
        Coin[] coins = Coin.VALUES;
        for (int i = coins.length - 1; i >= 0; i--) {
            Coin coin = coins[i];

            Couple<Integer> tuple = coin.convert(spurs);
            if (tuple.getFirst() != 0)
                bag.add(coin, tuple.getFirst());
            spurs = tuple.getSecond();
        }
        return bag;
    }

    public static DiscreteCoinBag ofChange(int costInSpurs, Coin coinToBreak) {
        return DiscreteCoinBag.ofGreedy(coinToBreak.value - costInSpurs);
    }

    public static DiscreteCoinBag of() {
        return new DiscreteCoinBag();
    }

    public void clear() {
        coins.clear();
    }

    public void dropContents(Level level, BlockPos pos) {
        dropContents(level, pos.getX(), pos.getY(), pos.getZ());
    }

    public void dropContents(Level level, Entity entityAt) {
        dropContents(level, entityAt.getX(), entityAt.getY(), entityAt.getZ());
    }

    private void dropContents(Level level, double x, double y, double z) {
        coins.forEach((coin, amount) -> Containers.dropItemStack(level, x, y, z, coin.asStack(amount)));
    }
}
