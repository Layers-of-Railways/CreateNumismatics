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

package dev.ithundxr.createnumismatics.forge.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity.InterfaceEnergyHandler;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyManager;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.annotation.mixin.ConditionalMixin;
import dev.ithundxr.createnumismatics.compat.Mods;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.EnergySalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.forge.InvalidatableWrappingEnergyBufferStorage;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.content.salepoint.types.Energy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@ConditionalMixin(mods = Mods.CREATEADDITION)
@Mixin(PortableEnergyInterfaceBlockEntity.class)
public abstract class PortableEnergyInterfaceBlockEntityMixin extends PortableStorageInterfaceBlockEntity {

    @Shadow(remap = false) protected LazyOptional<IEnergyStorage> capability;

    @Unique
    private EnergySalepointTargetBehaviour railway$salepointBehaviour;

    @Unique
    @Nullable
    private IEnergyStorage railway$contraptionStorage;

    private PortableEnergyInterfaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
    private void keepControl(Contraption contraption, float distance, CallbackInfo ci, @Local(name = "oldcap") LazyOptional<IEnergyStorage> oldcap) {
        railway$contraptionStorage = PortableEnergyManager.get(contraption);

        oldcap.ifPresent(energyHandler -> {
            IEnergyStorage existingWrapped = ((InterfaceEnergyHandlerAccessor) energyHandler).getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingEnergyBufferStorage) {
                capability.ifPresent(newEnergyHandler -> {
                    ((InterfaceEnergyHandlerAccessor) newEnergyHandler).setWrapped(existingWrapped);
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
    private void keepControl2(CallbackInfo ci, @Local(name = "oldcap") LazyOptional<IEnergyStorage> oldcap) {
        railway$contraptionStorage = null;

        oldcap.ifPresent(energyHandler -> {
            IEnergyStorage existingWrapped = ((InterfaceEnergyHandlerAccessor) energyHandler).getWrapped();
            if (existingWrapped instanceof InvalidatableWrappingEnergyBufferStorage) {
                capability.ifPresent(newEnergyHandler -> {
                    ((InterfaceEnergyHandlerAccessor) newEnergyHandler).setWrapped(existingWrapped);
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
        railway$salepointBehaviour = new EnergySalepointTargetBehaviour(this) {
            private boolean underControl;

            @Override
            protected boolean isUnderControlInternal(@NotNull ISalepointState<Energy> state) {
                return underControl; // id checks done by super
            }

            @Override
            protected void ensureUnderControlInternal(@NotNull ISalepointState<Energy> state) {
                capability.ifPresent(energyHandler -> {
                    ((InterfaceEnergyHandlerAccessor) energyHandler).setWrapped((InvalidatableWrappingEnergyBufferStorage) state.getBuffer());
                });

                if (!underControl) {
                    underControl = true;
                    notifyUpdate();
                }
            }

            @Override
            protected void relinquishControlInternal(@NotNull ISalepointState<Energy> state) {
                capability.ifPresent(energyHandler -> {
                    ((InterfaceEnergyHandlerAccessor) energyHandler).setWrapped(Objects.requireNonNullElseGet(
                        railway$contraptionStorage,
                        () -> new EnergyStorage(0)
                    ));
                });

                if (underControl) {
                    underControl = false;
                    notifyUpdate();
                }
            }

            @Override
            public boolean hasSpaceFor(@NotNull Energy object) {
                if (railway$contraptionStorage == null)
                    return false;

                if (!railway$contraptionStorage.canReceive())
                    return false;

                if (railway$contraptionStorage.receiveEnergy((int) object.getAmount(), true) == 0)
                    return false;

                int remainingCapacity = railway$contraptionStorage.getMaxEnergyStored() - railway$contraptionStorage.getEnergyStored();
                return remainingCapacity >= object.getAmount();
            }

            @Override
            public boolean doPurchase(@NotNull Energy object, @NotNull PurchaseProvider<Energy> purchaseProvider) {
                if (railway$contraptionStorage == null)
                    return false;

                if (!hasSpaceFor(object))
                    return false;

                List<Energy> extracted = purchaseProvider.extract();
                for (Energy energy : extracted) {
                    long totalInserted = 0;
                    while (totalInserted < energy.getAmount()) {
                        long inserted = railway$contraptionStorage.receiveEnergy((int) (energy.getAmount() - totalInserted), false);
                        if (inserted == 0)
                            break;
                        totalInserted += inserted;
                    }
                    if (totalInserted != energy.getAmount()) {
                        Numismatics.LOGGER.error("Failed to insert energy into contraption storage, despite having space.");
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

    @ConditionalMixin(mods = Mods.CREATEADDITION)
    @Mixin(InterfaceEnergyHandler.class)
    private interface InterfaceEnergyHandlerAccessor {
        @Accessor(value = "wrapped",remap = false)
        IEnergyStorage getWrapped();

        @Accessor(value = "wrapped", remap = false) @Mutable
        void setWrapped(IEnergyStorage wrapped);
    }
}
