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

package dev.ithundxr.createnumismatics.forge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.ItemSalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.forge.InvalidatableWrappingItemBufferHandler;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(PortableItemInterfaceBlockEntity.class)
public abstract class PortableItemInterfaceBlockEntityMixin extends PortableStorageInterfaceBlockEntity {
    @Shadow(remap = false) protected LazyOptional<IItemHandlerModifiable> capability;

    @Unique
    private ItemSalepointTargetBehaviour railway$salepointBehaviour;

    @Unique
    @Nullable
    private IItemHandlerModifiable railway$contraptionStorage;

    private PortableItemInterfaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(
        method = "startTransferringTo",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/util/LazyOptional;invalidate()V"
        ),
        remap = false
    )
    private void keepControl(Contraption contraption, float distance, CallbackInfo ci, @Local(name = "oldCap") LazyOptional<IItemHandlerModifiable> oldCap) {
        railway$contraptionStorage = contraption.getSharedInventory();

        oldCap.ifPresent(itemHandler -> {
            IItemHandlerModifiable existingWrapped = ((ItemHandlerWrapperAccessor) itemHandler).getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingItemBufferHandler) {
                capability.ifPresent(newItemHandler -> {
                    ((ItemHandlerWrapperAccessor) newItemHandler).setWrapped(existingWrapped);
                });
            }
        });
    }

    @Inject(
        method = "stopTransferring",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/util/LazyOptional;invalidate()V"
        ),
        remap = false
    )
    private void keepControl2(CallbackInfo ci, @Local(name = "oldCap") LazyOptional<IItemHandlerModifiable> oldCap) {
        railway$contraptionStorage = null;

        oldCap.ifPresent(itemHandler -> {
            IItemHandlerModifiable existingWrapped = ((ItemHandlerWrapperAccessor) itemHandler).getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingItemBufferHandler) {
                capability.ifPresent(newItemHandler -> {
                    ((ItemHandlerWrapperAccessor) newItemHandler).setWrapped(existingWrapped);
                });
            }
        });
    }

    @Override
    public boolean canTransfer() {
        return super.canTransfer() || railway$salepointBehaviour.isControlledBySalepoint();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        railway$salepointBehaviour = new ItemSalepointTargetBehaviour(this) {
            private boolean underControl = false;

            @Override
            protected boolean isUnderControlInternal(@NotNull ISalepointState<ItemStack> state) {
                return underControl; // id checks done by super
            }

            @Override
            protected void ensureUnderControlInternal(@NotNull ISalepointState<ItemStack> state) {
                capability.ifPresent(itemHandler -> {
                    ((ItemHandlerWrapperAccessor) itemHandler).setWrapped((InvalidatableWrappingItemBufferHandler) state.getBuffer());
                });

                if (!underControl) {
                    underControl = true;
                    notifyUpdate();
                }
            }

            @Override
            protected void relinquishControlInternal(@NotNull ISalepointState<ItemStack> state) {
                capability.ifPresent(itemHandler -> {
                    ((ItemHandlerWrapperAccessor) itemHandler).setWrapped(Objects.requireNonNullElseGet(
                        railway$contraptionStorage,
                        () -> new ItemStackHandler(0)
                    ));
                });

                if (underControl) {
                    underControl = false;
                    notifyUpdate();
                }
            }

            @Override
            public boolean hasSpaceFor(@NotNull ItemStack object) {
                if (railway$contraptionStorage == null)
                    return false;

                return ItemHandlerHelper.insertItem(railway$contraptionStorage, object, true).isEmpty();
            }

            @Override
            public boolean doPurchase(@NotNull ItemStack object, @NotNull PurchaseProvider<ItemStack> purchaseProvider) {
                if (railway$contraptionStorage == null)
                    return false;

                if (!hasSpaceFor(object))
                    return false;

                List<ItemStack> extracted = purchaseProvider.extract();
                for (ItemStack stack : extracted) {
                    if (!ItemHandlerHelper.insertItem(railway$contraptionStorage, stack, false).isEmpty()) {
                        Numismatics.LOGGER.error("Failed to insert item into contraption storage, despite having space.");
                        return false;
                    }
                }

                return true;
            }

            @Override
            public void read(@NotNull CompoundTag nbt, boolean clientPacket) {
                super.read(nbt, clientPacket);

                underControl = nbt.getBoolean("SalepointUnderControl");
            }

            @Override
            public void write(@NotNull CompoundTag nbt, boolean clientPacket) {
                super.write(nbt, clientPacket);

                nbt.putBoolean("SalepointUnderControl", underControl);
            }
        };

        behaviours.add(railway$salepointBehaviour);
    }
}
