/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.mixin.client;

import com.simibubi.create.content.trains.bogey.StandardBogeyBlockEntity;
import dev.ithundxr.createnumismatics.mixin_interfaces.StandardBogeyBlockEntity_Duck;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StandardBogeyBlockEntity.class)
public class MixinStandardBogeyBlockEntity implements StandardBogeyBlockEntity_Duck {
    @Unique
    private double numismatics$couplingDistance = -1;

    @Unique
    private Direction numismatics$couplingDirection = Direction.UP;

    @Unique
    private boolean numismatics$couplingFront = false;

    @Override
    public void numismatics$setCouplingDistance(double distance) {
        numismatics$couplingDistance = distance;
    }

    @Override
    public double numismatics$getCouplingDistance() {
        return numismatics$couplingDistance;
    }

    @Override
    public void numismatics$setCouplingDirection(Direction direction) {
        numismatics$couplingDirection = direction;
    }

    @Override
    public Direction numismatics$getCouplingDirection() {
        return numismatics$couplingDirection;
    }

    @Override
    public void numismatics$setFront(boolean front) {
        numismatics$couplingFront = front;
    }

    @Override
    public boolean numismatics$getFront() {
        return numismatics$couplingFront;
    }
}
