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

package dev.ithundxr.createnumismatics.content.backend;

import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ItemWritingContainer<SELF extends ItemWritingContainer<SELF>> implements Container {
    private final Consumer<SELF> slotsChangedCallback;

    @NotNull
    protected final List<ItemStack> stacks = new ArrayList<>();

    public ItemWritingContainer(Consumer<SELF> slotsChangedCallback) {
        this.slotsChangedCallback = slotsChangedCallback;
        stacks.add(ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    protected ItemStack getStack() {
        return stacks.get(0);
    }

    @Override
    public boolean isEmpty() {
        return getStack().isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return getStack();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.stacks, 0, amount);
        if (!stack.isEmpty()) {
            this.slotsChangedCallback.accept((SELF) this);
        }
        return stack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.stacks, 0);
    }

    protected abstract void doWriteItem(ItemStack stack);

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        this.stacks.set(0, stack);
        doWriteItem(stack);
        this.slotsChangedCallback.accept((SELF) this);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.stacks.set(0, ItemStack.EMPTY);
    }
}
