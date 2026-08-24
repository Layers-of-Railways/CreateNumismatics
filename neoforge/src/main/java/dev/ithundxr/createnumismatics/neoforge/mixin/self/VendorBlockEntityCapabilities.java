/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
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

package dev.ithundxr.createnumismatics.neoforge.mixin.self;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import dev.ithundxr.createnumismatics.neoforge.capability_ducks.VendorBlockEntity_Duck;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VendorBlockEntity.class)
public abstract class VendorBlockEntityCapabilities extends SmartBlockEntity implements VendorBlockEntity_Duck, WorldlyContainer {
    public VendorBlockEntityCapabilities(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow(remap = false) AbstractComputerBehaviour computerBehaviour;

    @Unique IItemHandler numismatics$handler = new SidedInvWrapper(this, Direction.NORTH);

    @Override
    public AbstractComputerBehaviour duck$getComputerBehaviour() {
        return computerBehaviour;
    }

    @Override
    public IItemHandler duck$getItemHandler() {
        return numismatics$handler;
    }
}
