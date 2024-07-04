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

package dev.ithundxr.createnumismatics.compat.computercraft;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.FallbackComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.compat.Mods;

import java.util.function.Function;

public class ComputerCraftProxy {
    public static void register() {
        fallbackFactory = FallbackComputerBehaviour::new;
        Mods.COMPUTERCRAFT.executeIfInstalled(() -> ComputerCraftProxy::registerWithDependency);
    }

    @ExpectPlatform
    static void registerWithDependency() {
        throw new AssertionError();
    }

    public static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> fallbackFactory;
    public static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> computerFactory;

    @ExpectPlatform
    public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
        throw new AssertionError();
    }
}