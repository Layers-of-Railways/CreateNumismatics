/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

public class SlotDiscreteCoinBag extends Slot {
    private static final Container emptyInventory = new SimpleContainer(0);
    private final DiscreteCoinBag coinBag;
    private final Coin coin;
    private final boolean canInsert;
    private final boolean canExtract;

    public SlotDiscreteCoinBag(DiscreteCoinBag coinBag, Coin coin, int x, int y, boolean canInsert, boolean canExtract) {
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

        return stack.getItem() instanceof CoinItem coinItem && coinItem.coin == coin;
    }

    @Override
    public @NotNull ItemStack getItem() {
        return coinBag.asStack(coin);
    }

    @Override
    public void set(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof CoinItem coinItem && coinItem.coin == coin) {
            coinBag.setDiscrete(coin, stack.getCount());
            setChanged();
        }
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;//NumismaticsItems.getCoin(coin).get().getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getMaxStackSize();
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        if (!canExtract)
            return false;

        return coinBag.getDiscrete(coin) > 0;
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        amount = Math.min(amount, coinBag.getDiscrete(coin));

        if (amount <= 0)
            return ItemStack.EMPTY;

        coinBag.subtract(coin, amount);
        return coin.asStack(amount);
    }

    /**
     * Tries to add a stack to the coin hoard
     * @param stack ItemStack to add, will be cleared if placing succeeds
     * @return success
     */
    public boolean tryPlace(ItemStack stack) {
        if (!mayPlace(stack)) return false;

        coinBag.add(coin, stack.getCount());
        stack.setCount(0);
        setChanged();

        return true;
    }

    @Override
    public @NotNull Optional<ItemStack> tryRemove(int count, int decrement, @NotNull Player player) {
        return super.tryRemove(count, Math.min(64, decrement), player);
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, Numismatics.asResource("item/coin/outline/"+coin.getName()));
    }
}
