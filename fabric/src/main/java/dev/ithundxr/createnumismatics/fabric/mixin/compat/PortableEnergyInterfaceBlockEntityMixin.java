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

package dev.ithundxr.createnumismatics.fabric.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity.InterfaceEnergyHandler;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.annotation.mixin.ConditionalMixin;
import dev.ithundxr.createnumismatics.compat.Mods;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.EnergySalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.fabric.InvalidatableWrappingEnergyBufferStorage;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.content.salepoint.types.Energy;
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
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.List;
import java.util.Objects;

@ConditionalMixin(mods = Mods.CREATEADDITION)
@Mixin(PortableEnergyInterfaceBlockEntity.class)
@SuppressWarnings("UnstableApiUsage")
public abstract class PortableEnergyInterfaceBlockEntityMixin extends PortableStorageInterfaceBlockEntity {
    @Shadow
    protected InterfaceEnergyHandler capability;

    @Unique
    private EnergySalepointTargetBehaviour railway$salepointBehaviour;

    @Unique
    @Nullable
    private EnergyStorage railway$contraptionStorage;

    private PortableEnergyInterfaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @WrapOperation(
        method = "startTransferringTo",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mrh0/createaddition/blocks/portable_energy_interface/PortableEnergyInterfaceBlockEntity$InterfaceEnergyHandler;setWrapped(Lteam/reborn/energy/api/EnergyStorage;)V"
        ),
        remap = false
    )
    private void keepControl(InterfaceEnergyHandler instance, EnergyStorage wrapped, Operation<Void> original) {
        EnergyStorage existingWrapped = ((InterfaceEnergyHandlerAccessor) instance).getWrapped();
        if (!(existingWrapped instanceof InvalidatableWrappingEnergyBufferStorage)) {
            original.call(instance, wrapped);
        }
        railway$contraptionStorage = ((InterfaceEnergyHandlerAccessor) wrapped).getWrapped();
    }

    @WrapOperation(
        method = "stopTransferring",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mrh0/createaddition/blocks/portable_energy_interface/PortableEnergyInterfaceBlockEntity$InterfaceEnergyHandler;setWrapped(Lteam/reborn/energy/api/EnergyStorage;)V"
        ),
        remap = false
    )
    private void keepControl2(InterfaceEnergyHandler instance, EnergyStorage wrapped, Operation<Void> original) {
        EnergyStorage existingWrapped = ((InterfaceEnergyHandlerAccessor) instance).getWrapped();
        if (!(existingWrapped instanceof InvalidatableWrappingEnergyBufferStorage)) {
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
        railway$salepointBehaviour = new EnergySalepointTargetBehaviour(this) {
            private boolean underControl = false;
            @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
            private boolean debug = false;

            @Override
            protected boolean isUnderControlInternal(@NotNull ISalepointState<Energy> state) {
                return underControl; // id checks done by super
            }

            @Override
            protected void ensureUnderControlInternal(@NotNull ISalepointState<Energy> state) {
                ((InterfaceEnergyHandlerAccessor) capability).setWrapped((InvalidatableWrappingEnergyBufferStorage) state.getBuffer());

                if (!underControl) {
                    underControl = true;
                    notifyUpdate();
                }
            }

            @Override
            protected void relinquishControlInternal(@NotNull ISalepointState<Energy> state) {
                ((InterfaceEnergyHandlerAccessor) capability).setWrapped(Objects.requireNonNullElseGet(
                    railway$contraptionStorage,
                    () -> new SimpleEnergyStorage(0, 0, 0)
                ));

                if (underControl) {
                    underControl = false;
                    notifyUpdate();
                }
            }

            @Override
            public boolean hasSpaceFor(@NotNull Energy object) {
                if (railway$contraptionStorage == null) {
                    if (debug) {
                        Numismatics.LOGGER.error("Contraption storage is null, cannot check for space.");
                    }
                    return false;
                }

                if (!railway$contraptionStorage.supportsInsertion()) {
                    if (debug) {
                        Numismatics.LOGGER.error("Contraption storage does not support insertion, cannot check for space.");
                    }
                    return false;
                }

                try (Transaction transaction = Transaction.openOuter()) {
                    long totalInserted = 0;
                    while (totalInserted < object.getAmount()) {
                        long inserted = railway$contraptionStorage.insert(object.getAmount() - totalInserted, transaction);
                        if (inserted == 0)
                            break;
                        totalInserted += inserted;
                    }
                    if (totalInserted != object.getAmount()) {
                        if (debug) {
                            Numismatics.LOGGER.error("Tried to insert {} energy, managed to insert {} energy.", object.getAmount(), totalInserted);
                        }
                        return false;
                    }
                }

                return true;
            }

            @Override
            public boolean doPurchase(@NotNull Energy object, @NotNull PurchaseProvider<Energy> purchaseProvider) {
                if (railway$contraptionStorage == null)
                    return false;

                if (!hasSpaceFor(object))
                    return false;

                List<Energy> extracted = purchaseProvider.extract();
                try (Transaction transaction = Transaction.openOuter()) {
                    for (Energy energy : extracted) {
                        long totalInserted = 0;
                        while (totalInserted < energy.getAmount()) {
                            long inserted = railway$contraptionStorage.insert(energy.getAmount() - totalInserted, transaction);
                            if (inserted == 0)
                                break;
                            totalInserted += inserted;
                        }
                        if (totalInserted != energy.getAmount()) {
                            Numismatics.LOGGER.error("Failed to insert energy into contraption storage, despite having space.");
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

    @Mixin(InterfaceEnergyHandler.class)
    private interface InterfaceEnergyHandlerAccessor {
        @Accessor("wrapped")
        EnergyStorage getWrapped();

        @Accessor("wrapped")
        void setWrapped(EnergyStorage wrapped);
    }
}
