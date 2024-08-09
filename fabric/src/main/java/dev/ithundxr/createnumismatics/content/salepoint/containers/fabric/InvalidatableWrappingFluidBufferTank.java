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

import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableAbstractBuffer;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.multiloader.fluid.fabric.MultiloaderFluidStackImpl;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class InvalidatableWrappingFluidBufferTank extends InvalidatableAbstractBuffer<MultiloaderFluidStack> implements Storage<FluidVariant> {

    protected FluidTank buffer;

    public InvalidatableWrappingFluidBufferTank(FluidTank buffer) {
        this.buffer = buffer;
    }

    @Override
    protected void afterInvalidate() {
        super.afterInvalidate();
        buffer = null;
    }

    @Override
    protected int copyToBufferInternal(MultiloaderFluidStack source, boolean simulate) {
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = buffer.insert(((MultiloaderFluidStackImpl) source).getType(), source.getAmount(), transaction);
            if (!simulate) {
                transaction.commit();
            }
            return (int) inserted;
        }
    }

    @Override
    protected int removeFromBufferInternal(MultiloaderFluidStack source, boolean simulate, int maxAmount) {
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = buffer.extract(((MultiloaderFluidStackImpl) source).getType(), maxAmount, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return (int) extracted;
        }
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (!isValid())
            return 0;

        return buffer.insert(resource, maxAmount, transaction);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (!isValid())
            return 0;

        return buffer.extract(resource, maxAmount, transaction);
    }

    @Override
    public @NotNull Iterator<StorageView<FluidVariant>> iterator() {
        if (!isValid())
            return Collections.emptyListIterator();

        return buffer.iterator();
    }
}
