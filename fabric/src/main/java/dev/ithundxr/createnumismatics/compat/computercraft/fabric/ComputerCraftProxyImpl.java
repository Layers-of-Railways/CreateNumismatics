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

package dev.ithundxr.createnumismatics.compat.computercraft.fabric;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import dev.ithundxr.createnumismatics.compat.computercraft.ComputerCraftProxy;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.ComputerBehaviour;

public class ComputerCraftProxyImpl {
    public static void registerWithDependency() {
        /* Comment if computercraft.implementation is not in the source set */
        ComputerCraftProxy.computerFactory = ComputerBehaviour::new;

        PeripheralLookup.get().registerFallback((level, blockPos, blockState, blockEntity, direction) -> ComputerBehaviour.peripheralProvider(level, blockPos));
    }

    public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
        if (ComputerCraftProxy.computerFactory == null)
            return ComputerCraftProxy.fallbackFactory.apply(sbe);
        return ComputerCraftProxy.computerFactory.apply(sbe);
    }
}
