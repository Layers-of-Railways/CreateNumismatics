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

package dev.ithundxr.createnumismatics.compat.computercraft.implementation.fabric;

import dan200.computercraft.api.detail.FabricDetailRegistries;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.multiloader.fluid.fabric.MultiloaderFluidStackImpl;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class DetailsImpl {
    public static Map<String, Object> getItemDetail(ItemStack stack) {
        return VanillaDetailRegistries.ITEM_STACK.getDetails(stack);
    }

    public static Map<String, Object> getFluidDetail(MultiloaderFluidStack stack) {
        return FabricDetailRegistries.FLUID_VARIANT.getDetails(new StorageView<>() {
            @Override
            public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
                return 0;
            }

            @Override
            public boolean isResourceBlank() {
                return false;
            }

            @Override
            public FluidVariant getResource() {
                return ((MultiloaderFluidStackImpl) stack).getType();
            }

            @Override
            public long getAmount() {
                return stack.getAmount();
            }

            @Override
            public long getCapacity() {
                return stack.getAmount();
            }
        });
    }
}
