/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.forge.mixin.self;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VendorBlockEntity.class)
public abstract class VendorBlockEntityCapabilities extends SmartBlockEntity implements ICapabilityProvider, WorldlyContainer {
    public VendorBlockEntityCapabilities(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow AbstractComputerBehaviour computerBehaviour;
    
    @Unique LazyOptional<? extends IItemHandler> numismatics$handler = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.NORTH));

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !remove) {
            // If down return the down handler otherwise return the one for all other sides
            return numismatics$handler.cast();
        }

        if (computerBehaviour.isPeripheralCap(capability))
            return computerBehaviour.getPeripheralCapability();

        return super.getCapability(capability, facing);
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        numismatics$handler = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.NORTH));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        computerBehaviour.removePeripheral();
        
        numismatics$handler.invalidate();
    }
}
