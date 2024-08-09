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

package dev.ithundxr.createnumismatics.content.salepoint.containers;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InvalidatableWrappingItemBuffer extends InvalidatableAbstractBuffer<ItemStack> {

    protected SimpleContainer buffer;

    public InvalidatableWrappingItemBuffer(SimpleContainer buffer) {
        this.buffer = buffer;
    }

    @Override
    protected void afterInvalidate() {
        super.afterInvalidate();
        buffer = null;
    }

    @Override
    protected int copyToBufferInternal(ItemStack source, boolean simulate) {
        final SimpleContainer buffer$ = this.buffer;
        SimpleContainer buffer = this.buffer;
        if (simulate) { // lazy but it works
            buffer = new SimpleContainer(buffer.getContainerSize()) {
                @Override
                public @NotNull ItemStack addItem(@NotNull ItemStack stack) {
                    if (!buffer$.canPlaceItem(0, stack))
                        return stack;

                    return super.addItem(stack);
                }

                @Override
                public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
                    return buffer$.canPlaceItem(index, stack);
                }
            };
            for (int i = 0; i < buffer.getContainerSize(); i++) {
                buffer.setItem(i, this.buffer.getItem(i).copy());
            }
        }

        ItemStack result = buffer.addItem(source.copy());
        if (source.getCount() != result.getCount()) {
            return source.getCount() - result.getCount();
        }
        return 0;
    }

    @Override
    protected int removeFromBufferInternal(ItemStack source, boolean simulate, final int maxAmount) {
        SimpleContainer buffer = this.buffer;
        if (simulate) { // lazy but it works
            buffer = new SimpleContainer(buffer.getContainerSize());
            for (int i = 0; i < buffer.getContainerSize(); i++) {
                buffer.setItem(i, this.buffer.getItem(i).copy());
            }
        }

        int total = 0;
        for (int slot = 0; slot < buffer.getContainerSize(); slot++) {
            ItemStack stack = buffer.getItem(slot);
            if (ItemStack.isSameItemSameTags(source, stack)) {
                int remainingAmount = maxAmount - total;
                int count = Math.min(remainingAmount, stack.getCount());
                ItemStack newStack = buffer.removeItem(slot, count);
                total += stack.getCount() - newStack.getCount();
            }
        }
        return total;
    }
}
