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

package dev.ithundxr.createnumismatics.neoforge.mixin.self;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import dev.ithundxr.createnumismatics.content.coins.neoforge.DiscreteCoinBagItemHandler;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlockEntity;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlockEntity;
import dev.ithundxr.createnumismatics.neoforge.capability_ducks.BrassDepositorBlockEntity_Duck;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BrassDepositorBlockEntity.class)
public abstract class BrassDepositorBlockEntityCapabilities extends AbstractDepositorBlockEntity implements BrassDepositorBlockEntity_Duck, DiscreteCoinBag.StorageTarget {
    public BrassDepositorBlockEntityCapabilities(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow(remap = false) AbstractComputerBehaviour computerBehaviour;

    @Unique IItemHandler numismatics$handler = new DiscreteCoinBagItemHandler(this);

    @Override
    public IItemHandler duck$getItemHandler() {
        return numismatics$handler;
    }

    @Override
    public AbstractComputerBehaviour duck$getComputerBehaviour() {
        return computerBehaviour;
    }
}
