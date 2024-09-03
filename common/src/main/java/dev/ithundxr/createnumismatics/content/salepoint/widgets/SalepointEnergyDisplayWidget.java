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

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import com.simibubi.create.foundation.gui.ILightingSettings;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.base.client.rendering.ISalepointStateUpdatingWidget;
import dev.ithundxr.createnumismatics.base.client.rendering.UIRenderHelper;
import dev.ithundxr.createnumismatics.content.salepoint.states.EnergySalepointState;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.content.salepoint.types.Energy;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SalepointEnergyDisplayWidget extends AbstractSimiWidget implements ISalepointStateUpdatingWidget {

    public static final ILightingSettings DEFAULT_LIGHTING = CustomLightingSettings.builder()
        .firstLightRotation(12.5f, 135.0f)
        .secondLightRotation(-20.0f, 140.0f)
        .build();

    protected @NotNull EnergySalepointState state;

    public SalepointEnergyDisplayWidget(int x, int y, @NotNull EnergySalepointState state) {
        super(x, y, 28, 28);
        this.state = state;
    }

    @Override
    protected void doRender(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getX();
        int y = getY();
        PoseStack ms = graphics.pose();

        NumismaticsGuiTextures.SALEPOINT_FLUID_BG.render(graphics, x, y);

        Energy filter = state.getFilter();
        {
            ms.pushPose();
            ms.translate(0, 0, 10);

            NumismaticsGuiTextures.SALEPOINT_ENERGY_BG.render(graphics, getX()+2, getY()+2);

            if (filter.getAmount() > 0) {
                float level = (float) filter.getAmount() / (float) EnergySalepointState.getFilterCapacity();
                int height = (int) (level * NumismaticsGuiTextures.SALEPOINT_ENERGY_FG.height);

                UIRenderHelper.drawCropped(graphics,
                    getX() + 3, getY() + 3,
                    0, NumismaticsGuiTextures.SALEPOINT_ENERGY_FG.height - height,
                    NumismaticsGuiTextures.SALEPOINT_ENERGY_FG.width, NumismaticsGuiTextures.SALEPOINT_ENERGY_FG.height,
                    5, NumismaticsGuiTextures.SALEPOINT_ENERGY_FG
                );
            }

            ms.popPose();
        }

        {
            ms.pushPose();
            ms.translate(0, 0, 20);

            NumismaticsGuiTextures.SALEPOINT_FLUID_FG.render(graphics, x, y);

            ms.popPose();
        }
    }

    @Override
    public void updateState(ISalepointState<?> state) {
        if (state instanceof EnergySalepointState energySalepointState)
            this.state = energySalepointState;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public List<Component> getToolTip() {
        Energy filter = state.getFilter();

        return List.of(
            Components.translatable("gui.numismatics.salepoint.energy"),
            Components.literal(TextUtils.formatEnergy(filter.getAmount()))
        );
    }
}
