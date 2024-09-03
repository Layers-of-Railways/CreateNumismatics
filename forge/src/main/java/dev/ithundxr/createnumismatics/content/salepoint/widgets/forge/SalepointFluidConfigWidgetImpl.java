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

package dev.ithundxr.createnumismatics.content.salepoint.widgets.forge;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.multiloader.fluid.forge.MultiloaderFluidStackImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SalepointFluidConfigWidgetImpl {
    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static MultiloaderFluidStack getFluidFrom(@NotNull ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        if (level == null)
            return null;

        if (!GenericItemEmptying.canItemBeEmptied(level, stack))
            return null;

        return new MultiloaderFluidStackImpl(GenericItemEmptying.emptyItem(level, stack, true).getFirst());
    }
}
