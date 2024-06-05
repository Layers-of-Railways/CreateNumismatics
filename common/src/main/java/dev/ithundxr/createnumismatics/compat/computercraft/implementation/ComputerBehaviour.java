/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.compat.computercraft.implementation;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.BrassDepositorPeripheral;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.VendorPeripheral;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class ComputerBehaviour extends AbstractComputerBehaviour {
    NonNullSupplier<IPeripheral> peripheralSupplier;

    public static IPeripheral peripheralProvider(Level level, BlockPos blockPos) {
        AbstractComputerBehaviour behavior = BlockEntityBehaviour.get(level, blockPos, AbstractComputerBehaviour.TYPE);
        if (behavior instanceof ComputerBehaviour real)
            return real.getPeripheral();
        return null;
    }

    public ComputerBehaviour(SmartBlockEntity te) {
        super(te);
        this.peripheralSupplier = getPeripheralFor(te);
    }

    public static NonNullSupplier<IPeripheral> getPeripheralFor(SmartBlockEntity be) {
        if (be instanceof BrassDepositorBlockEntity scbe)
            return () -> new BrassDepositorPeripheral(scbe);
        if (be instanceof VendorBlockEntity scbe)
            return () -> new VendorPeripheral(scbe);

        throw new IllegalArgumentException("No peripheral available for " + be.getType());
    }

    @Override
    public <T> T getPeripheral() {
        //noinspection unchecked
        return (T) peripheralSupplier.get();
    }
}
