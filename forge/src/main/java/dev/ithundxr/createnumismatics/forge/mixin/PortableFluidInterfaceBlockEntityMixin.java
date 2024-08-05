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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity.InterfaceFluidHandler;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.FluidSalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.SalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.forge.InvalidatableWrappingFluidBufferTank;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.multiloader.fluid.forge.MultiloaderFluidStackImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(PortableFluidInterfaceBlockEntity.class)
public abstract class PortableFluidInterfaceBlockEntityMixin extends PortableStorageInterfaceBlockEntity {

    @Shadow protected LazyOptional<IFluidHandler> capability;

    @Unique
    private FluidSalepointTargetBehaviour railway$salepointBehaviour;

    @Unique
    @Nullable
    private IFluidHandler railway$contraptionStorage;

    private PortableFluidInterfaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
    private void keepControl(Contraption contraption, float distance, CallbackInfo ci, @Local(name = "oldcap") LazyOptional<IFluidHandler> oldcap) {
        railway$contraptionStorage = contraption.getSharedFluidTanks();

        oldcap.ifPresent(fluidHandler -> {
            IFluidHandler existingWrapped = ((InterfaceFluidHandlerAccessor) fluidHandler).getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingFluidBufferTank) {
                capability.ifPresent(newFluidHandler -> {
                    ((InterfaceFluidHandlerAccessor) newFluidHandler).setWrapped(existingWrapped);
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
    private void keepControl2(CallbackInfo ci, @Local(name = "oldcap") LazyOptional<IFluidHandler> oldcap) {
        railway$contraptionStorage = null;

        oldcap.ifPresent(fluidHandler -> {
            IFluidHandler existingWrapped = ((InterfaceFluidHandlerAccessor) fluidHandler).getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingFluidBufferTank) {
                capability.ifPresent(newFluidHandler -> {
                    ((InterfaceFluidHandlerAccessor) newFluidHandler).setWrapped(existingWrapped);
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
        railway$salepointBehaviour = new FluidSalepointTargetBehaviour(this) {
            private boolean underControl = false;

            @Override
            protected boolean isUnderControlInternal(@NotNull ISalepointState<MultiloaderFluidStack> state) {
                return underControl;  // id checks done by super
            }

            @Override
            protected void ensureUnderControlInternal(@NotNull ISalepointState<MultiloaderFluidStack> state) {
                capability.ifPresent(fluidHandler -> {
                    ((InterfaceFluidHandlerAccessor) fluidHandler).setWrapped((InvalidatableWrappingFluidBufferTank) state.getBuffer());
                });

                if (!underControl) {
                    underControl = true;
                    notifyUpdate();
                }
            }

            @Override
            protected void relinquishControlInternal(@NotNull ISalepointState<MultiloaderFluidStack> state) {
                capability.ifPresent(fluidHandler -> {
                    ((InterfaceFluidHandlerAccessor) fluidHandler).setWrapped(Objects.requireNonNullElseGet(
                        railway$contraptionStorage,
                        () -> new FluidTank(0)
                    ));
                });

                if (underControl) {
                    underControl = false;
                    notifyUpdate();
                }
            }

            @Override
            public boolean hasSpaceFor(@NotNull MultiloaderFluidStack object) {
                if (railway$contraptionStorage == null)
                    return false;

                return railway$contraptionStorage.fill(((MultiloaderFluidStackImpl) object).getWrapped(), FluidAction.SIMULATE) == object.getAmount();
            }

            @Override
            public boolean doPurchase(@NotNull MultiloaderFluidStack object, @NotNull PurchaseProvider<MultiloaderFluidStack> purchaseProvider) {
                if (railway$contraptionStorage == null)
                    return false;

                if (!hasSpaceFor(object))
                    return false;

                List<MultiloaderFluidStack> extracted = purchaseProvider.extract();
                for (MultiloaderFluidStack fluidStack : extracted) {
                    if (railway$contraptionStorage.fill(((MultiloaderFluidStackImpl) fluidStack).getWrapped(), FluidAction.EXECUTE) != fluidStack.getAmount()) {
                        Numismatics.LOGGER.error("Failed to insert fluid into contraption storage, despite having space.");
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

    @Mixin(InterfaceFluidHandler.class)
    private interface InterfaceFluidHandlerAccessor {
        @Accessor("wrapped")
        IFluidHandler getWrapped();

        @Accessor("wrapped")
        void setWrapped(IFluidHandler wrapped);
    }

    @Mixin(InterfaceFluidHandler.class)
    private static class InterfaceFluidHandlerMixin {
        @WrapOperation(
            method = "fill",
            at = @At(
                value = "INVOKE",
                target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableFluidInterfaceBlockEntity;isConnected()Z"
            )
        )
        private boolean fakeConnect(PortableFluidInterfaceBlockEntity instance, Operation<Boolean> original) {
            return original.call(instance) || instance.getBehaviour(SalepointTargetBehaviour.TYPE).isControlledBySalepoint();
        }
    }
}
