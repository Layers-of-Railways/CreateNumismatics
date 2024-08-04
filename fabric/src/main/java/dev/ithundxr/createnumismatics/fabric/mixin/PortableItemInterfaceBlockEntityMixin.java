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
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHandlerWrapper;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.ItemSalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.fabric.InvalidatableWrappingItemBufferStorage;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(PortableItemInterfaceBlockEntity.class)
public abstract class PortableItemInterfaceBlockEntityMixin extends PortableStorageInterfaceBlockEntity {
    @Shadow public abstract Storage<ItemVariant> getItemStorage(@Nullable Direction face);

    @Unique
    private ItemSalepointTargetBehaviour railway$salepointBehaviour;

    @Unique
    @Nullable
    private Storage<ItemVariant> railway$contraptionStorage;

    private PortableItemInterfaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapOperation(
        method = "startTransferringTo",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableItemInterfaceBlockEntity$InterfaceItemHandler;setWrapped(Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;)V"
        ),
        remap = false
    )
    private void keepControl(@Coerce ItemHandlerWrapper instance, Storage<ItemVariant> wrapped, Operation<Void> original) {
        Storage<ItemVariant> existingWrapped = ((ItemHandlerWrapperAccessor) instance).getWrapped();
        if (!(existingWrapped instanceof InvalidatableWrappingItemBufferStorage)) { // don't override a controlled buffer
            original.call(instance, wrapped);
        }
        railway$contraptionStorage = wrapped;
    }

    @WrapOperation(
        method = "stopTransferring",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableItemInterfaceBlockEntity$InterfaceItemHandler;setWrapped(Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;)V"
        ),
        remap = false
    )
    private void keepControl2(@Coerce ItemHandlerWrapper instance, Storage<ItemVariant> wrapped, Operation<Void> original) {
        Storage<ItemVariant> existingWrapped = ((ItemHandlerWrapperAccessor) instance).getWrapped();
        if (!(existingWrapped instanceof InvalidatableWrappingItemBufferStorage)) { // don't override a controlled buffer
            original.call(instance, wrapped);
        }
        railway$contraptionStorage = null;
    }

    @Override
    public boolean canTransfer() {
        return super.canTransfer() || railway$salepointBehaviour.isControlledBySalepoint();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) { // no transfer because no entity so sad.
        super.addBehaviours(behaviours);
        railway$salepointBehaviour = new ItemSalepointTargetBehaviour(this) {
            private boolean underControl = false;

            @Override
            protected boolean isUnderControlInternal(@NotNull ISalepointState<ItemStack> state) {
                return underControl; // id checks done by super
            }

            @Override
            protected void ensureUnderControlInternal(@NotNull ISalepointState<ItemStack> state) {
                ((ItemHandlerWrapperAccessor) getItemStorage(null)).setWrapped((InvalidatableWrappingItemBufferStorage) state.getBuffer());

                if (!underControl) {
                    underControl = true;
                    notifyUpdate();
                }
            }

            @Override
            protected void relinquishControlInternal(@NotNull ISalepointState<ItemStack> state) {
                if (railway$contraptionStorage != null) {
                    ((ItemHandlerWrapperAccessor) getItemStorage(null)).setWrapped(railway$contraptionStorage);
                } else {
                    ((ItemHandlerWrapperAccessor) getItemStorage(null)).setWrapped(Storage.empty());
                }

                if (underControl) {
                    underControl = false;
                    notifyUpdate();
                }
            }

            @Override
            public boolean hasSpaceFor(@NotNull ItemStack object) {
                if (railway$contraptionStorage == null)
                    return false;

                if (!railway$contraptionStorage.supportsInsertion())
                    return false;

                try (Transaction transaction = Transaction.openOuter()) {
                    if (railway$contraptionStorage.insert(ItemVariant.of(object), object.getCount(), transaction) != object.getCount()) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public boolean doPurchase(@NotNull ItemStack object, @NotNull PurchaseProvider<ItemStack> purchaseProvider) {
                if (railway$contraptionStorage == null)
                    return false;

                if (!hasSpaceFor(object))
                    return false;

                try (Transaction transaction = Transaction.openOuter()) {
                    List<ItemStack> extracted = purchaseProvider.extract();
                    for (ItemStack stack : extracted) {
                        if (railway$contraptionStorage.insert(ItemVariant.of(stack), stack.getCount(), transaction) != stack.getCount()) {
                            Numismatics.LOGGER.error("Failed to insert item into contraption storage, despite having space.");
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
}
