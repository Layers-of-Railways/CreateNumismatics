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

package dev.ithundxr.createnumismatics.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity.InterfaceFluidHandler;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.FluidSalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.SalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.fabric.InvalidatableWrappingFluidBufferTank;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.multiloader.fluid.fabric.MultiloaderFluidStackImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PortableFluidInterfaceBlockEntity.class)
@SuppressWarnings("UnstableApiUsage")
public abstract class PortableFluidInterfaceBlockEntityMixin extends PortableStorageInterfaceBlockEntity {

    @Shadow protected InterfaceFluidHandler capability;
    @Unique
    private FluidSalepointTargetBehaviour railway$salepointBehaviour;

    @Unique
    @Nullable
    private Storage<FluidVariant> railway$contraptionStorage;

    private PortableFluidInterfaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapOperation(
        method = "startTransferringTo",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableFluidInterfaceBlockEntity$InterfaceFluidHandler;setWrapped(Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;)V"
        ),
        remap = false
    )
    @SuppressWarnings("unchecked")
    private void keepControl(InterfaceFluidHandler instance, Storage<FluidVariant> wrapped, Operation<Void> original) {
        Storage<FluidVariant> existingWrapped = ((WrappedStorageAccessor<FluidVariant>) capability).getWrapped();
        if (!(existingWrapped instanceof InvalidatableWrappingFluidBufferTank)) {
            original.call(instance, wrapped);
        }
        railway$contraptionStorage = wrapped;
    }

    @WrapOperation(
        method = "stopTransferring",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableFluidInterfaceBlockEntity$InterfaceFluidHandler;setWrapped(Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;)V"
        ),
        remap = false
    )
    @SuppressWarnings("unchecked")
    private void keepControl2(InterfaceFluidHandler instance, Storage<FluidVariant> wrapped, Operation<Void> original) {
        Storage<FluidVariant> existingWrapped = ((WrappedStorageAccessor<FluidVariant>) capability).getWrapped();
        if (!(existingWrapped instanceof InvalidatableWrappingFluidBufferTank)) {
            original.call(instance, wrapped);
        }
        railway$contraptionStorage = null;
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
                return underControl; // id checks done by super
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void ensureUnderControlInternal(@NotNull ISalepointState<MultiloaderFluidStack> state) {
                ((WrappedStorageAccessor<FluidVariant>) capability).setWrapped((InvalidatableWrappingFluidBufferTank) state.getBuffer());

                if (!underControl) {
                    underControl = true;
                    notifyUpdate();
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void relinquishControlInternal(@NotNull ISalepointState<MultiloaderFluidStack> state) {
                if (railway$contraptionStorage != null) {
                    ((WrappedStorageAccessor<FluidVariant>) capability).setWrapped(railway$contraptionStorage);
                } else {
                    ((WrappedStorageAccessor<FluidVariant>) capability).setWrapped(Storage.empty());
                }

                if (underControl) {
                    underControl = false;
                    notifyUpdate();
                }
            }

            @Override
            public boolean hasSpaceFor(@NotNull MultiloaderFluidStack object) {
                if (railway$contraptionStorage == null)
                    return false;

                if (!railway$contraptionStorage.supportsInsertion())
                    return false;

                try (Transaction transaction = Transaction.openOuter()) {
                    if (railway$contraptionStorage.insert(((MultiloaderFluidStackImpl) object).getType(), object.getAmount(), transaction) != object.getAmount()) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public boolean doPurchase(@NotNull MultiloaderFluidStack object, @NotNull PurchaseProvider<MultiloaderFluidStack> purchaseProvider) {
                if (railway$contraptionStorage == null)
                    return false;

                if (!hasSpaceFor(object))
                    return false;

                List<MultiloaderFluidStack> extracted = purchaseProvider.extract();
                try (Transaction transaction = Transaction.openOuter()) {
                    for (MultiloaderFluidStack stack : extracted) {
                        if (railway$contraptionStorage.insert(((MultiloaderFluidStackImpl) stack).getType(), stack.getAmount(), transaction) != stack.getAmount()) {
                            Numismatics.LOGGER.error("Failed to insert fluid into contraption storage, despite having space.");
                            return false;
                        }
                    }
                    transaction.commit();
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
    private static class InterfaceFluidHandlerMixin {
        @WrapOperation(
            method = "insert(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;JLnet/fabricmc/fabric/api/transfer/v1/transaction/TransactionContext;)J",
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
