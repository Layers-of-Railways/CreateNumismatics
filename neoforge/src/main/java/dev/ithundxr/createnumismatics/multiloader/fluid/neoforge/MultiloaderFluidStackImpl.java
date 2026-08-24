/*
 * Numismatics
 * Copyright (c) 2024-2026 The Railways Team
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

package dev.ithundxr.createnumismatics.multiloader.fluid.neoforge;

import com.mojang.serialization.Codec;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiloaderFluidStackImpl extends MultiloaderFluidStack {

    public static Codec<MultiloaderFluidStack> makeCodec() {
        return FluidStack.CODEC.xmap(MultiloaderFluidStackImpl::new, fs -> ((MultiloaderFluidStackImpl) fs).wrapped);
    }

    public static Codec<MultiloaderFluidStack> makeOptionalCodec() {
        return FluidStack.OPTIONAL_CODEC.xmap(MultiloaderFluidStackImpl::new, fs -> ((MultiloaderFluidStackImpl) fs).wrapped);
    }

    private static StreamCodec<RegistryFriendlyByteBuf, MultiloaderFluidStack> makeStreamCodec() {
        return FluidStack.STREAM_CODEC.map(MultiloaderFluidStackImpl::new, fs -> ((MultiloaderFluidStackImpl) fs).wrapped);
    }

    private static StreamCodec<RegistryFriendlyByteBuf, MultiloaderFluidStack> makeOptionalStreamCodec() {
        return FluidStack.OPTIONAL_STREAM_CODEC.map(MultiloaderFluidStackImpl::new, fs -> ((MultiloaderFluidStackImpl) fs).wrapped);
    }

    public static MultiloaderFluidStack makeEmpty() {
        return new MultiloaderFluidStackImpl(FluidStack.EMPTY);
    }

    private final FluidStack wrapped;

    public MultiloaderFluidStackImpl(FluidStack wrapped) {
        this.wrapped = wrapped;
    }

    public MultiloaderFluidStackImpl(Fluid fluid, int amount) {
        this(new FluidStack(fluid, amount));
    }

    public MultiloaderFluidStackImpl(Fluid fluid, int amount, @NotNull DataComponentPatch patch) {
        this(new FluidStack(Holder.direct(fluid), amount, patch));
    }

    public MultiloaderFluidStackImpl(FluidStack stack, int amount) {
        this(stack.copyWithAmount(amount));
    }

    public MultiloaderFluidStackImpl(MultiloaderFluidStack stack, int amount) {
        this(((MultiloaderFluidStackImpl) stack).wrapped, amount);
    }

    @Override
    public MultiloaderFluidStack setAmount(long amount) {
        wrapped.setAmount((int) amount);
        return this;
    }

    @Override
    public Fluid getFluid() {
        return wrapped.getFluid();
    }

    @Override
    public long getAmount() {
        return wrapped.getAmount();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public boolean isFluidEqual(MultiloaderFluidStack other) {
        return FluidStack.isSameFluidSameComponents(wrapped, ((MultiloaderFluidStackImpl) other).wrapped);
    }

    @Override
    public Component getHoverName() {
        return wrapped.getHoverName();
    }

    @Override
    public MultiloaderFluidStack copy() {
        return new MultiloaderFluidStackImpl(wrapped.copy());
    }

    @Override
    public boolean isFluidStackIdentical(MultiloaderFluidStack other) {
        return FluidStack.matches(wrapped, ((MultiloaderFluidStackImpl) other).wrapped);
    }

    @Override
    public boolean isFluidEqual(@NotNull ItemStack other) {
        return FluidUtil.getFluidContained(other)
            .map(other1 -> FluidStack.isSameFluidSameComponents(wrapped, other1))
            .orElse(false);
    }

    @Override
    public boolean isLighterThanAir() {
        return getFluid().getFluidType().isLighterThanAir();
    }

    @Override
    public final int hashCode() {
        return wrapped.hashCode();
    }

    /**
     * Default equality comparison for a FluidStack. Same functionality as isFluidEqual().
     *
     * This is included for use in data structures.
     */
    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof MultiloaderFluidStack fs))
            return false;

        return isFluidEqual(fs);
    }

    public static MultiloaderFluidStack create(Fluid fluid, long amount, @Nullable DataComponentPatch patch) {
        return new MultiloaderFluidStackImpl(fluid, (int) amount, patch == null ? DataComponentPatch.EMPTY : patch);
    }

    public FluidStack getWrapped() {
        return wrapped;
    }

    @Override
    public <T> @Nullable T set(@NotNull DataComponentType<? super T> component, @Nullable T value) {
        return wrapped.set(component, value);
    }

    @Override
    public <T> @Nullable T remove(@NotNull DataComponentType<? extends T> component) {
        return wrapped.remove(component);
    }

    @Override
    public void applyComponents(@NotNull DataComponentPatch components) {
        wrapped.applyComponents(components);
    }

    @Override
    public void applyComponents(@NotNull DataComponentMap components) {
        wrapped.applyComponents(components);
    }

    @Override
    public @NotNull DataComponentMap getComponents() {
        return wrapped.getComponents();
    }
}
