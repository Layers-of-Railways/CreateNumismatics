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
