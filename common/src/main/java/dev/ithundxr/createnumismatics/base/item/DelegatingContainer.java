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

package dev.ithundxr.createnumismatics.base.item;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class DelegatingContainer implements Container {

    protected abstract Container getContainer();

    @Override
    public int getContainerSize() {
        Container container = getContainer();
        return container == null ? 0 : container.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        Container container = getContainer();
        return container == null || container.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        Container container = getContainer();
        return container == null ? ItemStack.EMPTY : container.getItem(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        Container container = getContainer();
        return container == null ? ItemStack.EMPTY : container.removeItem(slot, amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        Container container = getContainer();
        return container == null ? ItemStack.EMPTY : container.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        Container container = getContainer();
        if (container != null) {
            container.setItem(slot, stack);
        }
    }

    @Override
    public void setChanged() {
        Container container = getContainer();
        if (container != null) {
            container.setChanged();
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        Container container = getContainer();
        return container != null && container.stillValid(player);
    }

    @Override
    public void clearContent() {
        Container container = getContainer();
        if (container != null) {
            container.clearContent();
        }
    }
}
