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
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.ItemSalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.neoforge.InvalidatableWrappingItemBufferHandler;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Objects;

@Mixin(PortableItemInterfaceBlockEntity.class)
public abstract class PortableItemInterfaceBlockEntityMixin extends PortableStorageInterfaceBlockEntity {
    @Shadow(remap = false) protected IItemHandlerModifiable capability;

    @Unique
    private ItemSalepointTargetBehaviour numismatics$salepointBehaviour;

    @Unique
    @Nullable
    private IItemHandlerModifiable numismatics$contraptionStorage;

    private PortableItemInterfaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapOperation(
        method = "startTransferringTo",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableItemInterfaceBlockEntity;capability:Lnet/neoforged/neoforge/items/IItemHandlerModifiable;",
            opcode = Opcodes.PUTFIELD
        ),
        remap = false
    )
    private void keepControl(
        PortableItemInterfaceBlockEntity instance,
        IItemHandlerModifiable value,
        Operation<Void> original,
        Contraption contraption
    ) {
        numismatics$contraptionStorage = contraption.getStorage().getAllItems();

        if (capability instanceof ItemHandlerWrapperAccessor oldWrapper && value instanceof ItemHandlerWrapperAccessor newWrapper) {
            IItemHandlerModifiable existingWrapped = oldWrapper.getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingItemBufferHandler) {
                newWrapper.setWrapped(existingWrapped);
            }
        }

        original.call(instance, value);
    }

    @WrapOperation(
        method = "stopTransferring",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableItemInterfaceBlockEntity;capability:Lnet/neoforged/neoforge/items/IItemHandlerModifiable;",
            opcode = Opcodes.PUTFIELD
        ),
        remap = false
    )
    private void keepControl2(PortableItemInterfaceBlockEntity instance, IItemHandlerModifiable value, Operation<Void> original) {
        numismatics$contraptionStorage = null;

        if (capability instanceof ItemHandlerWrapperAccessor oldWrapper && value instanceof ItemHandlerWrapperAccessor newWrapper) {
            IItemHandlerModifiable existingWrapped = oldWrapper.getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingItemBufferHandler) {
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
        numismatics$salepointBehaviour = new ItemSalepointTargetBehaviour(this) {
            private boolean underControl = false;

            @Override
            protected boolean isUnderControlInternal(@NotNull ISalepointState<ItemStack> state) {
                return underControl; // id checks done by super
            }

            @Override
            protected void ensureUnderControlInternal(@NotNull ISalepointState<ItemStack> state) {
                if (capability instanceof ItemHandlerWrapperAccessor wrapper) {
                    wrapper.setWrapped((InvalidatableWrappingItemBufferHandler) state.getBuffer());
                }

                if (!underControl) {
                    underControl = true;
                    notifyUpdate();
                }
            }

            @Override
            protected void relinquishControlInternal(@NotNull ISalepointState<ItemStack> state) {
                if (capability instanceof ItemHandlerWrapperAccessor wrapper) {
                    wrapper.setWrapped(Objects.requireNonNullElseGet(
                        numismatics$contraptionStorage,
                        () -> new ItemStackHandler(0)
                    ));
                }

                if (underControl) {
                    underControl = false;
                    notifyUpdate();
                }
            }

            @Override
            public boolean hasSpaceFor(@NotNull ItemStack object) {
                if (numismatics$contraptionStorage == null)
                    return false;

                return ItemHandlerHelper.insertItem(numismatics$contraptionStorage, object, true).isEmpty();
            }

            @Override
            public boolean doPurchase(@NotNull ItemStack object, @NotNull PurchaseProvider<ItemStack> purchaseProvider) {
                if (numismatics$contraptionStorage == null)
                    return false;

                if (!hasSpaceFor(object))
                    return false;

                List<ItemStack> extracted = purchaseProvider.extract();
                for (ItemStack stack : extracted) {
                    if (!ItemHandlerHelper.insertItem(numismatics$contraptionStorage, stack, false).isEmpty()) {
                        Numismatics.LOGGER.error("Failed to insert item into contraption storage, despite having space.");
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
}
