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

package dev.ithundxr.createnumismatics.neoforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity.InterfaceFluidHandler;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.FluidSalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.SalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.neoforge.InvalidatableWrappingFluidBufferTank;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.multiloader.fluid.neoforge.MultiloaderFluidStackImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Objects;

@Mixin(PortableFluidInterfaceBlockEntity.class)
public abstract class PortableFluidInterfaceBlockEntityMixin extends PortableStorageInterfaceBlockEntity {

    @Shadow(remap = false) protected IFluidHandler capability;

    @Unique
    private FluidSalepointTargetBehaviour numismatics$salepointBehaviour;

    @Unique
    @Nullable
    private IFluidHandler numismatics$contraptionStorage;

    private PortableFluidInterfaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapOperation(
        method = "startTransferringTo",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableFluidInterfaceBlockEntity;capability:Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;",
            opcode = Opcodes.PUTFIELD
        ),
        remap = false
    )
    private void keepControl(
        PortableFluidInterfaceBlockEntity instance,
        IFluidHandler value,
        Operation<Void> original,
        Contraption contraption
    ) {
        numismatics$contraptionStorage = contraption.getStorage().getFluids();

        if (capability instanceof InterfaceFluidHandlerAccessor oldWrapper && value instanceof InterfaceFluidHandlerAccessor newWrapper) {
            IFluidHandler existingWrapped = oldWrapper.getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingFluidBufferTank) {
                newWrapper.setWrapped(existingWrapped);
            }
        }

        original.call(instance, value);
    }

    @WrapOperation(
        method = "stopTransferring",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableFluidInterfaceBlockEntity;capability:Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;",
            opcode = Opcodes.PUTFIELD
        ),
        remap = false
    )
    private void keepControl2(PortableFluidInterfaceBlockEntity instance, IFluidHandler value, Operation<Void> original) {
        numismatics$contraptionStorage = null;

        if (capability instanceof InterfaceFluidHandlerAccessor oldWrapper && value instanceof InterfaceFluidHandlerAccessor newWrapper) {
            IFluidHandler existingWrapped = oldWrapper.getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingFluidBufferTank) {
                newWrapper.setWrapped(existingWrapped);
            }
        }

        original.call(instance, value);
    }

    @Override
    public boolean canTransfer() {
        return super.canTransfer() || numismatics$salepointBehaviour.isControlledBySalepoint();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        numismatics$salepointBehaviour = new FluidSalepointTargetBehaviour(this) {
            private boolean underControl = false;

            @Override
            protected boolean isUnderControlInternal(@NotNull ISalepointState<MultiloaderFluidStack> state) {
                return underControl;  // id checks done by super
            }

            @Override
            protected void ensureUnderControlInternal(@NotNull ISalepointState<MultiloaderFluidStack> state) {
                if (capability instanceof InterfaceFluidHandlerAccessor wrapper) {
                    wrapper.setWrapped((InvalidatableWrappingFluidBufferTank) state.getBuffer());
                }

                if (!underControl) {
                    underControl = true;
                    notifyUpdate();
                }
            }

            @Override
            protected void relinquishControlInternal(@NotNull ISalepointState<MultiloaderFluidStack> state) {
                if (capability instanceof InterfaceFluidHandlerAccessor wrapper) {
                    wrapper.setWrapped(Objects.requireNonNullElseGet(
                        numismatics$contraptionStorage,
                        () -> new FluidTank(0)
                    ));
                }

                if (underControl) {
                    underControl = false;
                    notifyUpdate();
                }
            }

            @Override
            public boolean hasSpaceFor(@NotNull MultiloaderFluidStack object) {
                if (numismatics$contraptionStorage == null)
                    return false;

                return numismatics$contraptionStorage.fill(((MultiloaderFluidStackImpl) object).getWrapped(), FluidAction.SIMULATE) == object.getAmount();
            }

            @Override
            public boolean doPurchase(@NotNull MultiloaderFluidStack object, @NotNull PurchaseProvider<MultiloaderFluidStack> purchaseProvider) {
                if (numismatics$contraptionStorage == null)
                    return false;

                if (!hasSpaceFor(object))
                    return false;

                List<MultiloaderFluidStack> extracted = purchaseProvider.extract();
                for (MultiloaderFluidStack fluidStack : extracted) {
                    if (numismatics$contraptionStorage.fill(((MultiloaderFluidStackImpl) fluidStack).getWrapped(), FluidAction.EXECUTE) != fluidStack.getAmount()) {
                        Numismatics.LOGGER.error("Failed to insert fluid into contraption storage, despite having space.");
                        return false;
                    }
                }

                return true;
            }

            @Override
            public void read(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries, boolean clientPacket) {
                super.read(nbt, registries, clientPacket);

                underControl = nbt.getBoolean("SalepointUnderControl");
            }

            @Override
            public void write(@NotNull CompoundTag nbt, HolderLookup.@NotNull Provider registries, boolean clientPacket) {
                super.write(nbt, registries, clientPacket);

                nbt.putBoolean("SalepointUnderControl", underControl);
            }
        };

        behaviours.add(numismatics$salepointBehaviour);
    }

    @Mixin(InterfaceFluidHandler.class)
    private interface InterfaceFluidHandlerAccessor {
        @Accessor(value = "wrapped", remap = false)
        IFluidHandler getWrapped();

        @Accessor(value = "wrapped", remap = false)
        void setWrapped(IFluidHandler wrapped);
    }

    @Mixin(InterfaceFluidHandler.class)
    private static class InterfaceFluidHandlerMixin {
        @WrapOperation(
            method = "fill",
            at = @At(
                value = "INVOKE",
                target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableFluidInterfaceBlockEntity;isConnected()Z"
            ),
            remap = false
        )
        private boolean fakeConnect(PortableFluidInterfaceBlockEntity instance, Operation<Boolean> original) {
            return original.call(instance) || instance.getBehaviour(SalepointTargetBehaviour.TYPE).isControlledBySalepoint();
        }
    }
}
