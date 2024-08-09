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

import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableAbstractBuffer;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.multiloader.fluid.forge.MultiloaderFluidStackImpl;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class InvalidatableWrappingFluidBufferTank extends InvalidatableAbstractBuffer<MultiloaderFluidStack> implements IFluidHandler {

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
        return buffer.fill(((MultiloaderFluidStackImpl) source).getWrapped(), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
    }

    @Override
    protected int removeFromBufferInternal(MultiloaderFluidStack source, boolean simulate, int maxAmount) {
        return buffer.drain(((MultiloaderFluidStackImpl) source).getWrapped(), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE).getAmount();
    }

    @Override
    public int getTanks() {
        if (!isValid())
            return 0;

        return buffer.getTanks();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (!isValid())
            return FluidStack.EMPTY;

        return buffer.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        if (!isValid())
            return 0;

        return buffer.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (!isValid())
            return false;

        return buffer.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!isValid())
            return 0;

        return buffer.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (!isValid())
            return FluidStack.EMPTY;

        return buffer.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int tank, FluidAction action) {
        if (!isValid())
            return FluidStack.EMPTY;

        return buffer.drain(tank, action);
    }

    /*    @Override
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
    }*/
}
