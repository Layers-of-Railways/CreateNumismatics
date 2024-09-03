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

package dev.ithundxr.createnumismatics.content.salepoint.widgets.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.multiloader.fluid.fabric.MultiloaderFluidStackImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

public class SalepointFluidDisplayWidgetImpl {
    @Environment(EnvType.CLIENT)
    public static void renderFluidBox(@NotNull MultiloaderFluidStack fluidStack, float xMin, float yMin, float zMin,
                                      float xMax, float yMax, float zMax, @NotNull MultiBufferSource buffer,
                                      @NotNull PoseStack ms, int light, boolean renderBottom) {
        FluidRenderer.renderFluidBox(((MultiloaderFluidStackImpl) fluidStack).getWrapped(), xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light, renderBottom);
    }
}
