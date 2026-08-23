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

package dev.ithundxr.createnumismatics.compat.computercraft.implementation;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.events.ComputerEvent;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.BrassDepositorPeripheral;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.SalepointPeripheral;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.VendorPeripheral;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlockEntity;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ComputerBehaviour extends AbstractComputerBehaviour {
    SyncedPeripheral<?> peripheral;
    Supplier<SyncedPeripheral<?>> peripheralSupplier;

    public ComputerBehaviour(SmartBlockEntity te) {
        super(te);
        this.peripheralSupplier = getPeripheralFor(te);
    }

    public static Supplier<SyncedPeripheral<?>> getPeripheralFor(SmartBlockEntity be) {
        if (be instanceof BrassDepositorBlockEntity scbe)
            return () -> new BrassDepositorPeripheral(scbe);
        if (be instanceof VendorBlockEntity scbe)
            return () -> new VendorPeripheral(scbe);
        if (be instanceof SalepointBlockEntity sbe)
            return () -> new SalepointPeripheral(sbe);

        throw new IllegalArgumentException("No peripheral available for " + be.getType());
    }

    @Override
    public IPeripheral getPeripheralCapability() {
        if (peripheral == null)
            peripheral = peripheralSupplier.get();
        return peripheral;
    }

    @ApiStatus.Internal
    @ExpectPlatform
    public static void removePeripheral(ComputerBehaviour behaviour) {
        throw new AssertionError();
    }

    @Override
    public void removePeripheral() {
        if (peripheral != null)
            removePeripheral(this);
    }

    @Override
    public void prepareComputerEvent(@NotNull ComputerEvent event) {
        if (peripheral != null)
            peripheral.prepareComputerEvent(event);
    }
}
