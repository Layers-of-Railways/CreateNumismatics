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

import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableWrappingItemBuffer;
import dev.ithundxr.createnumismatics.util.ItemUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.SimpleContainer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class InvalidatableWrappingItemBufferStorage extends InvalidatableWrappingItemBuffer implements Storage<ItemVariant> {

    protected final SnapshotParticipantImpl snapshotParticipant = new SnapshotParticipantImpl();

    public InvalidatableWrappingItemBufferStorage(SimpleContainer buffer) {
        super(buffer);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!isValid())
            return 0;

        snapshotParticipant.updateSnapshots(transaction);
        long amount = maxAmount;

        while (amount > 0) {
            int inserted = copyToBufferInternal(resource.toStack((int) Math.min(amount, resource.getItem().getMaxStackSize())), false);
            if (inserted == 0) {
                break;
            }
            amount -= inserted;
        }
        return maxAmount - amount;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!isValid())
            return 0;

        snapshotParticipant.updateSnapshots(transaction);
        long amount = maxAmount;
        while (amount > 0) {
            int extracted = removeFromBufferInternal(resource.toStack((int) Math.min(amount, resource.getItem().getMaxStackSize())), false, (int) Math.min(amount, Integer.MAX_VALUE));
            if (extracted == 0) {
                break;
            }
            amount -= extracted;
        }
        return maxAmount - amount;
    }

    @Override
    public @NotNull Iterator<StorageView<ItemVariant>> iterator() {
        if (!isValid())
            return Collections.emptyListIterator();

        return ReadWriteStackView.of(buffer)
            .<StorageView<ItemVariant>>map(view -> view)
            .iterator();
    }

    protected class SnapshotParticipantImpl extends SnapshotParticipant<SimpleContainer> {

        @Override
        protected SimpleContainer createSnapshot() {
            return ItemUtil.copy(buffer);
        }

        @Override
        protected void readSnapshot(SimpleContainer snapshot) {
            if (ItemUtil.copyInto(snapshot, buffer))
                buffer.setChanged();
        }
    }
}
