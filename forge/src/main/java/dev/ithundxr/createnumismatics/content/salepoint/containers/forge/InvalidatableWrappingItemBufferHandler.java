/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.content.salepoint.containers.forge;

import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableWrappingItemBuffer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class InvalidatableWrappingItemBufferHandler extends InvalidatableWrappingItemBuffer implements IItemHandlerModifiable {
    public InvalidatableWrappingItemBufferHandler(SimpleContainer buffer) {
        super(buffer);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (!isValid() || slot >= getSlots())
            throw new RuntimeException("Invalid slot");

        buffer.setItem(slot, stack);
    }

    @Override
    public int getSlots() {
        if (!isValid())
            return 0;

        return buffer.getContainerSize();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (!isValid() || slot >= getSlots())
            return ItemStack.EMPTY;

        return buffer.getItem(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!isValid() || slot >= getSlots())
            return stack;

        int inserted = copyToBufferInternal(stack, simulate);
        if (inserted == 0)
            return stack;

        return stack.copyWithCount(stack.getCount() - inserted);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!isValid() || slot >= getSlots())
            return ItemStack.EMPTY;

        if (simulate) {
            ItemStack stack = buffer.getItem(slot);
            return stack.copyWithCount(Math.min(amount, stack.getCount()));
        } else {
            return buffer.removeItem(slot, amount);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (!isValid() || slot >= getSlots())
            return 0;

        return buffer.getItem(slot).getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (!isValid() || slot >= getSlots())
            return false;

        return buffer.canPlaceItem(slot, stack);
    }
}
