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

package dev.ithundxr.createnumismatics.content.salepoint.containers.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class ReadWriteStackView extends SnapshotParticipant<Integer> implements StorageView<ItemVariant> {
    private final ItemVariant resource;
    private int count;
    private final ItemStack backingStack;
    private final int capacity;
    private final Runnable onChanged;

    private ReadWriteStackView(ItemVariant resource, int count, ItemStack backingStack, int capacity, Runnable onChanged) {
        this.resource = resource;
        this.count = count;
        this.backingStack = backingStack;
        this.capacity = capacity;
        this.onChanged = onChanged;
    }

    public static Stream<ReadWriteStackView> of(Container container) {
        List<ReadWriteStackView> out = new ArrayList<>();

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemVariant variant = ItemVariant.of(container.getItem(i));
            if (variant.isBlank()) {
                continue;
            }
            out.add(new ReadWriteStackView(variant, container.getItem(i).getCount(), container.getItem(i), variant.getItem().getMaxStackSize(), container::setChanged));
        }

        return out.stream();
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        updateSnapshots(transaction);

        if (resource.equals(this.resource)) {
            int extracted = (int) Math.min(count, maxAmount);
            count -= extracted;
            transaction.addCloseCallback((context, result) -> {
                if (result.wasCommitted()) {
                    backingStack.setCount(count);
                    onChanged.run();
                }
            });
            return extracted;
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return resource.isBlank();
    }

    @Override
    public ItemVariant getResource() {
        return resource;
    }

    @Override
    public long getAmount() {
        return count;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    protected Integer createSnapshot() {
        return count;
    }

    @Override
    protected void readSnapshot(Integer snapshot) {
        count = snapshot;
    }
}
