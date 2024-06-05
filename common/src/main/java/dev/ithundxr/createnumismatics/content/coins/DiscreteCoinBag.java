/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.content.coins;

import com.simibubi.create.foundation.utility.Couple;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
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

    @Override
    public int getValue() {
        return value;
    }

    public boolean isEmpty() {
        return value == 0;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        for (Map.Entry<Coin, Integer> entry : coins.entrySet()) {
            if (entry.getValue() > 0) {
                nbt.putInt(entry.getKey().name(), entry.getValue());
            }
        }
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        coins.clear();
        for (Coin coin : Coin.values()) {
            if (nbt.contains(coin.name())) {
                coins.put(coin, nbt.getInt(coin.name()));
            }
        }
        calculateValue();
    }

    public static DiscreteCoinBag of(CompoundTag nbt) {
        DiscreteCoinBag bag = new DiscreteCoinBag();
        bag.load(nbt);
        return bag;
    }

    public static DiscreteCoinBag of(Map<Coin, Integer> coins) {
        return new DiscreteCoinBag(coins);
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
        coins.forEach((coin, amount) -> {
            Containers.dropItemStack(level, x, y, z, coin.asStack(amount));
        });
    }
}
