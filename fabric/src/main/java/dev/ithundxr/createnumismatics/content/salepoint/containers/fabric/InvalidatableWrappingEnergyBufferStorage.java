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

package dev.ithundxr.createnumismatics.content.salepoint.containers.fabric;

import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableWrappingEnergyBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.types.EnergyBuffer;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.MethodsReturnNonnullByDefault;
import team.reborn.energy.api.EnergyStorage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("UnstableApiUsage")
public class InvalidatableWrappingEnergyBufferStorage extends InvalidatableWrappingEnergyBuffer implements EnergyStorage {

    private final SnapshotParticipant<Long> snapshotParticipant = new SnapshotParticipant<>() {
        private Long lastValue;

        @Override
        protected Long createSnapshot() {
            return buffer.getAmount();
        }

        @Override
        protected void readSnapshot(Long snapshot) {
            buffer.setAmountNoUpdate(snapshot);
        }

        @Override
        protected void onFinalCommit() {
            super.onFinalCommit();
            if (lastValue == null || lastValue != buffer.getAmount()) {
                buffer.setChanged();
                lastValue = buffer.getAmount();
            }
        }
    };

    public InvalidatableWrappingEnergyBufferStorage(EnergyBuffer buffer) {
        super(buffer);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        if (!isValid())
            return 0;

        snapshotParticipant.updateSnapshots(transaction);

        long amount = Math.min(maxAmount, buffer.getCapacity() - buffer.getAmount());
        buffer.setAmountNoUpdate(buffer.getAmount() + amount);

        return amount;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        if (!isValid())
            return 0;

        snapshotParticipant.updateSnapshots(transaction);

        long amount = Math.min(maxAmount, buffer.getAmount());
        buffer.setAmountNoUpdate(buffer.getAmount() - amount);

        return amount;
    }

    @Override
    public long getAmount() {
        if (!isValid())
            return 0;

        return buffer.getAmount();
    }

    @Override
    public long getCapacity() {
        if (!isValid())
            return 0;

        return buffer.getCapacity();
    }
}
