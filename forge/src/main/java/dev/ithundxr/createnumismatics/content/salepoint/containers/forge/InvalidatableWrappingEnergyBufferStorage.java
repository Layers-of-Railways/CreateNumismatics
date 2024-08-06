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

package dev.ithundxr.createnumismatics.content.salepoint.containers.forge;

import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableWrappingEnergyBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.types.Energy;
import dev.ithundxr.createnumismatics.content.salepoint.types.EnergyBuffer;
import net.minecraftforge.energy.IEnergyStorage;

public class InvalidatableWrappingEnergyBufferStorage extends InvalidatableWrappingEnergyBuffer implements IEnergyStorage {
    public InvalidatableWrappingEnergyBufferStorage(EnergyBuffer buffer) {
        super(buffer);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!isValid())
            return 0;

        return copyToBufferInternal(new Energy(maxReceive), simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!isValid())
            return 0;

        return removeFromBufferInternal(new Energy(maxExtract), simulate, maxExtract);
    }

    @Override
    public int getEnergyStored() {
        if (!isValid())
            return 0;

        return (int) buffer.getAmount();
    }

    @Override
    public int getMaxEnergyStored() {
        if (!isValid())
            return 0;

        return (int) buffer.getCapacity();
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
