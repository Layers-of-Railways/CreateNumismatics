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

package dev.ithundxr.createnumismatics.content.salepoint.widgets;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import com.simibubi.create.foundation.gui.ILightingSettings;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;
import com.simibubi.create.foundation.utility.Components;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.base.client.rendering.ISalepointStateUpdatingWidget;
import dev.ithundxr.createnumismatics.content.salepoint.states.FluidSalepointState;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SalepointFluidDisplayWidget extends AbstractSimiWidget implements ISalepointStateUpdatingWidget {

    public static final ILightingSettings DEFAULT_LIGHTING = CustomLightingSettings.builder()
        .firstLightRotation(12.5f, 135.0f)
        .secondLightRotation(-20.0f, 140.0f)
        .build();

    protected @NotNull FluidSalepointState state;

    public SalepointFluidDisplayWidget(int x, int y, @NotNull FluidSalepointState state) {
        super(x, y, 28, 28);
        this.state = state;
    }

    protected NumismaticsGuiTextures getBackground() {
        return NumismaticsGuiTextures.SALEPOINT_PURCHASE_FLUID_BG;
    }

    protected NumismaticsGuiTextures getForeground() {
        return NumismaticsGuiTextures.SALEPOINT_PURCHASE_FLUID_FG;
    }

    @Override
    protected void doRender(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getX();
        int y = getY();
        PoseStack ms = graphics.pose();

        getBackground().render(graphics, x, y);

        MultiloaderFluidStack filter = state.getFilter();
        if (!filter.isEmpty()) {
            ms.pushPose();
            ms.translate(0, 0, 10);

            boolean top = filter.isLighterThanAir();

            float level = (float) filter.getAmount() / (float) FluidSalepointState.getFilterCapacity();

            float xMin = x;
            float xMax = x + getWidth();
            float yMin = y + (1.0f - level) * (getHeight() - 4) + 2;
            float yMax = y + getHeight() - 2;

            if (top) {
                yMin = y + 2;
                yMax = y + level * (getHeight() - 4) + 2;
            }

            float zMin = 1;
            float zMax = 9;

            xMin /= 16f;
            xMax /= 16f;
            yMin /= 16f;
            yMax /= 16f;
            zMin /= 16f;
            zMax /= 16f;

            {
                DEFAULT_LIGHTING.applyLighting();

                ms.pushPose();
                ms.scale(16, 16, 16);

                float xOffset = (xMin + xMax) / 2;
                float yOffset = (yMin + yMax) / 2;
                float zOffset = (zMin + zMax) / 2;

                ms.translate(xOffset, yOffset, zOffset);

                xMin -= xOffset;
                xMax -= xOffset;
                yMin -= yOffset;
                yMax -= yOffset;
                zMin -= zOffset;
                zMax -= zOffset;

                DEFAULT_LIGHTING.applyLighting();

                renderFluidBox(filter, xMin, yMin, zMin, xMax, yMax, zMax, graphics.bufferSource(), ms, LightTexture.FULL_BRIGHT, false);
                graphics.bufferSource().endBatch();

                ms.popPose();

                Lighting.setupFor3DItems();
            }

            ms.popPose();
        }

        {
            ms.pushPose();
            ms.translate(0, 0, 20);

            getForeground().render(graphics, x, y);

            ms.popPose();
        }
    }

    @Override
    public void updateState(ISalepointState<?> state) {
        if (state instanceof FluidSalepointState fluidSalepointState)
            this.state = fluidSalepointState;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public List<Component> getToolTip() {
        MultiloaderFluidStack filter = state.getFilter();
        if (filter.isEmpty())
            return List.of(
                Components.translatable("gui.numismatics.salepoint.fluid_empty")
            );

        return List.of(
            filter.getDisplayName(),
            Components.literal(TextUtils.formatFluid(filter.getAmount()))
        );
    }

    @ExpectPlatform
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("SameParameterValue")
    protected static void renderFluidBox(@NotNull MultiloaderFluidStack fluidStack, float xMin, float yMin, float zMin,
                                       float xMax, float yMax, float zMax, @NotNull MultiBufferSource buffer,
                                       @NotNull PoseStack ms, int light, boolean renderBottom) {
        throw new AssertionError();
    }
}
