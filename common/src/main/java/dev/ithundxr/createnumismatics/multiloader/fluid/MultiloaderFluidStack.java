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

package dev.ithundxr.createnumismatics.multiloader.fluid;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.common.util.DataComponentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public abstract class MultiloaderFluidStack implements MutableDataComponentHolder {

    @ExpectPlatform
    private static Codec<MultiloaderFluidStack> makeCodec() {
        throw new AssertionError();
    }

    @ExpectPlatform
    private static Codec<MultiloaderFluidStack> makeOptionalCodec() {
        throw new AssertionError();
    }

    @ExpectPlatform
    private static StreamCodec<RegistryFriendlyByteBuf, MultiloaderFluidStack> makeStreamCodec() {
        throw new AssertionError();
    }

    @ExpectPlatform
    private static StreamCodec<RegistryFriendlyByteBuf, MultiloaderFluidStack> makeOptionalStreamCodec() {
        throw new AssertionError();
    }

    @ExpectPlatform
    private static MultiloaderFluidStack makeEmpty() {
        throw new AssertionError();
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<MultiloaderFluidStack> CODEC = makeCodec();
    public static final Codec<MultiloaderFluidStack> OPTIONAL_CODEC = makeOptionalCodec();

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiloaderFluidStack> STREAM_CODEC = makeStreamCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, MultiloaderFluidStack> OPTIONAL_STREAM_CODEC = makeOptionalStreamCodec();

    public static final MultiloaderFluidStack EMPTY = makeEmpty();

    public static MultiloaderFluidStack create(Fluid fluid, long amount) {
        return create(fluid, amount, null);
    }

    @ExpectPlatform
    public static MultiloaderFluidStack create(Fluid fluid, long amount, @Nullable DataComponentPatch patch) {
        throw new AssertionError();
    }

    public abstract MultiloaderFluidStack setAmount(long amount);

    public void grow(long amount) {
        setAmount(getAmount() + amount);
    }

    public abstract Fluid getFluid();

    public abstract long getAmount();

    public abstract boolean isEmpty();

    public void shrink(int amount) {
        setAmount(getAmount() - amount);
    }

    public void shrink(long amount) {
        setAmount(getAmount() - amount);
    }

    /**
     * Determines if the FluidIDs and NBT Tags are equal. This does not check amounts.
     *
     * @param other
     *            The FluidStack for comparison
     * @return true if the Fluids (IDs and NBT Tags) are the same
     */
    public abstract boolean isFluidEqual(MultiloaderFluidStack other);

    /**
     * Saves this stack to a tag, directly writing the keys into the passed tag.
     *
     * @throws IllegalStateException if this stack is empty
     */
    public Tag save(HolderLookup.Provider lookupProvider, Tag prefix) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty FluidStack");
        } else {
            return DataComponentUtil.wrapEncodingExceptions(this, CODEC, lookupProvider, prefix);
        }
    }

    /**
     * Saves this stack to a new tag.
     *
     * @throws IllegalStateException if this stack is empty
     */
    public Tag save(HolderLookup.Provider lookupProvider) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty FluidStack");
        } else {
            return DataComponentUtil.wrapEncodingExceptions(this, CODEC, lookupProvider);
        }
    }

    /**
     * Saves this stack to a new tag. Empty stacks are supported and will be saved as an empty tag.
     */
    public Tag saveOptional(HolderLookup.Provider lookupProvider) {
        return this.isEmpty() ? new CompoundTag() : this.save(lookupProvider, new CompoundTag());
    }

    /**
     * Tries to parse a fluid stack. Empty stacks cannot be parsed with this method.
     */
    public static Optional<MultiloaderFluidStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
        return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
            .resultOrPartial(error -> LOGGER.error("Tried to load invalid fluid: '{}'", error));
    }

    /**
     * Tries to parse a fluid stack, defaulting to {@link #EMPTY} on parsing failure.
     */
    public static MultiloaderFluidStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
        return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
    }

    public abstract Component getHoverName();

    public abstract MultiloaderFluidStack copy();

    /**
     * Determines if the Fluids are equal and this stack is larger.
     *
     * @return true if this FluidStack contains the other FluidStack (same fluid and >= amount)
     */
    public final boolean containsFluid(@NotNull MultiloaderFluidStack other) {
        return this.isFluidEqual(other) && this.getAmount() >= other.getAmount();
    }

    /**
     * Determines if the FluidIDs, Amounts, and NBT Tags are all equal.
     *
     * @param other
     *            - the FluidStack for comparison
     * @return true if the two FluidStacks are exactly the same
     */
    public abstract boolean isFluidStackIdentical(MultiloaderFluidStack other);

    /**
     * Determines if the FluidIDs and NBT Tags are equal compared to a registered container
     * ItemStack. This does not check amounts.
     *
     * @param other
     *            The ItemStack for comparison
     * @return true if the Fluids (IDs and NBT Tags) are the same
     */
    public abstract boolean isFluidEqual(@NotNull ItemStack other);

    public abstract boolean isLighterThanAir();
}
