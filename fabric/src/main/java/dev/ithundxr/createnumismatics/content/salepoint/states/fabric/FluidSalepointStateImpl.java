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

package dev.ithundxr.createnumismatics.content.salepoint.states.fabric;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableAbstractBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.containers.fabric.InvalidatableWrappingFluidBufferTank;
import dev.ithundxr.createnumismatics.content.salepoint.states.FluidSalepointState;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.multiloader.fluid.fabric.MultiloaderFluidStackImpl;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("UnstableApiUsage")
public class FluidSalepointStateImpl extends FluidSalepointState {

    private final @NotNull FluidTank buffer = new SmartFluidTank(4 * getFilterCapacity(), $ -> this.setChanged())
        .setValidator(fs -> this.getFilter().isFluidEqual(new MultiloaderFluidStackImpl(fs)));
    private @NotNull InvalidatableAbstractBuffer<MultiloaderFluidStack> bufferWrapper = createBufferWrapper(buffer);

    private static InvalidatableAbstractBuffer<MultiloaderFluidStack> createBufferWrapper(FluidTank buffer) {
        return new InvalidatableWrappingFluidBufferTank(buffer);
    }

    public static FluidSalepointState create() {
        return new FluidSalepointStateImpl();
    }

    @Override
    protected boolean canChangeFilterToInternal(MultiloaderFluidStack filter) {
        return buffer.isEmpty();
    }

    @Override
    protected void setFilterInternal(MultiloaderFluidStack filter, Level salepointLevel, BlockPos salepointPos, @Nullable Player player) {
        // buffer gets cleared when filter is set, but the filter *should* only be set when the buffer is empty
        if (!getFilter().isFluidEqual(filter))
            buffer.setFluid(FluidStack.EMPTY);
    }

    @Override
    protected void saveInternal(CompoundTag tag) {
        CompoundTag bufferTag = new CompoundTag();
        buffer.writeToNBT(bufferTag);
        tag.put("Buffer", bufferTag);
    }

    @Override
    protected void loadInternal(CompoundTag tag) {
        buffer.setFluid(FluidStack.EMPTY);

        if (tag.contains("Buffer", Tag.TAG_COMPOUND)) {
            buffer.readFromNBT(tag.getCompound("Buffer"));
        }
    }

    @Override
    protected boolean hasBufferFluidForPurchase() {
        return getFilter().isFluidEqual(new MultiloaderFluidStackImpl(buffer.getFluid()))
            && buffer.getAmount() >= getFilter().getAmount();
    }

    @Override
    protected List<MultiloaderFluidStack> removeBufferFluidForPurchase() {
        try (Transaction transaction = Transaction.openOuter()) {
            FluidStack out = buffer.getFluid().copy();

            long amount = buffer.extract(((MultiloaderFluidStackImpl) getFilter()).getType(), getFilter().getAmount(), transaction);
            transaction.commit();

            return List.of(
                new MultiloaderFluidStackImpl(out.setAmount(amount))
            );
        }
    }

    @Override
    public InvalidatableAbstractBuffer<MultiloaderFluidStack> getBuffer() {
        return bufferWrapper;
    }

    @Override
    public void onDestroy(Level level, BlockPos pos) {
        onUnload();
        buffer.setFluid(FluidStack.EMPTY);
    }

    @Override
    public void onUnload() {
        bufferWrapper.invalidate();
    }

    @Override
    public void keepAlive() {
        if (!bufferWrapper.isValid())
            bufferWrapper = createBufferWrapper(buffer);
    }
}
