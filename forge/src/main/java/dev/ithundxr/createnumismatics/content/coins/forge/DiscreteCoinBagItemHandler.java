/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.content.coins.forge;

import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class DiscreteCoinBagItemHandler implements IItemHandlerModifiable {
    private static final int CAPACITY = 65535;

    private final @NotNull DiscreteCoinBag.SidedStorageParams params;
    private final @NotNull DiscreteCoinBag coins;
    private final @NotNull Runnable setChanged;

    public DiscreteCoinBagItemHandler(DiscreteCoinBag.StorageTarget target) {
        this(target.$discreteCoinBag$getParams(), target.$discreteCoinBag$getDiscreteCoinBag(), target::$discreteCoinBag$setChanged);
    }

    public DiscreteCoinBagItemHandler(DiscreteCoinBag.@NotNull SidedStorageParams params, @NotNull DiscreteCoinBag coins, @NotNull Runnable setChanged) {
        this.params = params;
        this.coins = coins;
        this.setChanged = setChanged;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        Coin coin = Coin.values()[slot];
        if (!(stack.getItem() instanceof CoinItem coinItem && coinItem.coin == coin))
            throw new RuntimeException("Invalid item " + stack + " for coin slot " + coin);

        coins.setDiscrete(coin, stack.getCount());
        setChanged.run();
    }

    @Override
    public int getSlots() {
        return Coin.values().length;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= Coin.values().length)
            return ItemStack.EMPTY;
        Coin coin = Coin.values()[slot];
        return coin.asStack(coins.getDiscrete(coin));
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= Coin.values().length)
            return stack; // we return what is rejected

        if (!params.allowInsertion())
            return stack;

        Coin coin = Coin.values()[slot];
        if (!(stack.getItem() instanceof CoinItem coinItem && coinItem.coin == coin))
            return stack;

        int remainingCapacity = CAPACITY - coins.getDiscrete(coin);
        if (remainingCapacity <= 0)
            return stack;

        int inserted = Math.min(remainingCapacity, stack.getCount());

        if (!simulate) {
            coins.add(coin, inserted);
        }

        return inserted >= stack.getCount()
            ? ItemStack.EMPTY
            : stack.copyWithCount(stack.getCount() - inserted);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int maxAmount, boolean simulate) {
        if (slot < 0 || slot >= Coin.values().length)
            return ItemStack.EMPTY;

        if (!params.allowExtraction())
            return ItemStack.EMPTY;

        Coin coin = Coin.values()[slot];

        int available = coins.getDiscrete(coin);
        int extracted = Math.min(Math.min(available, maxAmount), 64);

        if (!simulate) {
            coins.setDiscrete(coin, available - extracted);
        }

        return coin.asStack(extracted);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < 0 || slot >= Coin.values().length)
            return 0;
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot < 0 || slot >= Coin.values().length)
            return false;

        if (!params.allowInsertion())
            return false;

        Coin coin = Coin.values()[slot];

        return stack.getItem() instanceof CoinItem coinItem && coinItem.coin == coin;
    }
}
