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

package dev.ithundxr.createnumismatics.forge.mixin.self;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.ComputerBehaviour;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ComputerBehaviour.class)
public class ComputerBehaviourCapabilities extends AbstractComputerBehaviour {
    public ComputerBehaviourCapabilities(SmartBlockEntity te) {
        super(te);
    }

    private static final Capability<IPeripheral> RAILWAYS$PERIPHERAL_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    LazyOptional<IPeripheral> railways$peripheral;
    @Shadow NonNullSupplier<IPeripheral> peripheralSupplier;

    @Override
    public <T> boolean isPeripheralCap(Capability<T> cap) {
        return cap == RAILWAYS$PERIPHERAL_CAPABILITY;
    }

    @Override
    public <T> LazyOptional<T> getPeripheralCapability() {
        if (railways$peripheral == null || !railways$peripheral.isPresent())
            railways$peripheral = LazyOptional.of(() -> peripheralSupplier.get());
        return railways$peripheral.cast();
    }

    @Override
    public void removePeripheral() {
        if (railways$peripheral != null)
            railways$peripheral.invalidate();
    }
}
