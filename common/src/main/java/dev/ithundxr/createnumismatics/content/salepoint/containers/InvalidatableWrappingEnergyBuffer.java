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

package dev.ithundxr.createnumismatics.content.salepoint.containers;

import dev.ithundxr.createnumismatics.content.salepoint.types.Energy;
import dev.ithundxr.createnumismatics.content.salepoint.types.EnergyBuffer;

public class InvalidatableWrappingEnergyBuffer extends InvalidatableAbstractBuffer<Energy> {

    protected EnergyBuffer buffer;

    public InvalidatableWrappingEnergyBuffer(EnergyBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    protected void afterInvalidate() {
        super.afterInvalidate();
        buffer = null;
    }

    @Override
    protected int copyToBufferInternal(Energy source, boolean simulate) {
        long amount = Math.min(source.getAmount(), buffer.getCapacity() - buffer.getAmount());
        if (!simulate) {
            buffer.setAmount(buffer.getAmount() + amount);
        }
        return (int) amount;
    }

    @Override
    protected int removeFromBufferInternal(Energy source, boolean simulate, int maxAmount) {
        long amount = Math.min(maxAmount, buffer.getAmount());

        if (!simulate) {
            buffer.setAmount(buffer.getAmount() - amount);
        }

        return (int) amount;
    }
}
