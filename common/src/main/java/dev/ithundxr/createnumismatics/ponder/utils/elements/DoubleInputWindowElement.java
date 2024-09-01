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

package dev.ithundxr.createnumismatics.ponder.utils.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.ponder.PonderLocalization;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.element.AnimatedOverlayElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.Pointing;
import dev.ithundxr.createnumismatics.mixin.client.AccessorInputWindowElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class DoubleInputWindowElement extends InputWindowElement {
    private final Vec3 sceneSpace;
    private final Pointing direction;
    InputWindowElement firstElement;
    InputWindowElement secondElement;

    public DoubleInputWindowElement clone() {
        return new DoubleInputWindowElement(sceneSpace, direction, firstElement.clone(), secondElement.clone());
    }

    public DoubleInputWindowElement(Vec3 sceneSpace, Pointing direction, InputWindowElement firstElement, InputWindowElement secondElement) {
        super(sceneSpace, direction);
        this.sceneSpace = sceneSpace;
        this.direction = direction;
        this.firstElement = firstElement;
        this.secondElement = secondElement;
    }

    @Override
    protected void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade) {
        Font font = screen.getFontRenderer();
        int width = 0;
        int height = 0;

        float xFade = direction == Pointing.RIGHT ? -1 : direction == Pointing.LEFT ? 1 : 0;
        float yFade = direction == Pointing.DOWN ? -1 : direction == Pointing.UP ? 1 : 0;
        xFade *= 10 * (1 - fade);
        yFade *= 10 * (1 - fade);

        ItemStack item1 = ((AccessorInputWindowElement) firstElement).numsismatics$getItem();
        ResourceLocation key1 = ((AccessorInputWindowElement) firstElement).numsismaticsgetKey();
        AllIcons icon1 = ((AccessorInputWindowElement) firstElement).numsismatics$getIcon();
        ItemStack item2 = ((AccessorInputWindowElement) secondElement).numsismatics$getItem();
        ResourceLocation key2 = ((AccessorInputWindowElement) secondElement).numsismaticsgetKey();
        AllIcons icon2 = ((AccessorInputWindowElement) secondElement).numsismatics$getIcon();
        
        boolean hasItem1 = !item1.isEmpty();
        boolean hasText1 = key1 != null;
        boolean hasIcon1 = icon1 != null;
        boolean hasItem2 = !item2.isEmpty();
        boolean hasText2 = key2 != null;
        boolean hasIcon2 = icon2 != null;
        
        int keyWidth1 = 0;
        int keyWidth2 = 0;
        
        String text1 = hasText1 ? PonderLocalization.getShared(key1) : "";
        String text2 = hasText2 ? PonderLocalization.getShared(key2) : "";

        if (fade < 1 / 16f)
            return;
        Vec2 sceneToScreen = scene.getTransform()
                .sceneToScreen(sceneSpace, partialTicks);

        if (hasIcon1) {
            width += 24;
            height = 24;
        }

        if (hasIcon2) {
            width += 24;
            height = 24;
        }

        if (hasText1) {
            keyWidth1 = font.width(text1);
            width += keyWidth1;
        }

        if (hasText2) {
            keyWidth2 = font.width(text2);
            width += keyWidth2;
        }

        if (hasItem1) {
            width += 24;
            height = 24;
        }

        if (hasItem2) {
            width += 24;
            height = 24;
        }


        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(sceneToScreen.x + xFade, sceneToScreen.y + yFade, 400);

        PonderUI.renderSpeechBox(graphics, 0, 0, width, height, false, direction, true);

        ms.translate(0, 0, 100);

        if (hasText1)
            graphics.drawString(font, text1, 2, (int) ((height - font.lineHeight) / 2f + 2),
                    PonderPalette.WHITE.getColorObject().scaleAlpha(fade).getRGB(), false);

        if (hasText2)
            graphics.drawString(font, text2, 4 + 24 + 8, (int) ((height - font.lineHeight) / 2f + 2),
                    PonderPalette.WHITE.getColorObject().scaleAlpha(fade).getRGB(), false);

        if (hasIcon1) {
            ms.pushPose();
            ms.translate(keyWidth1, 0, 0);
            ms.scale(1.5f, 1.5f, 1.5f);
            icon1.render(graphics, 0, 0);
            ms.popPose();
        }

        if (hasIcon2) {
            ms.pushPose();
            ms.translate(keyWidth2, 0, 0);
            ms.scale(1.5f, 1.5f, 1.5f);
            icon2.render(graphics, 24, 0);
            ms.popPose();
        }

        if (hasItem1) {
            GuiGameElement.of(item1)
                    .<GuiGameElement.GuiRenderBuilder>at(keyWidth1 + (hasIcon1 ? 24 : 0), 0)
                    .scale(1.5)
                    .render(graphics);
            RenderSystem.disableDepthTest();
        }

        if (hasItem2) {
            GuiGameElement.of(item2)
                    .<GuiGameElement.GuiRenderBuilder>at(keyWidth2 + (hasIcon2 ? 48 : 0), 0)
                    .scale(1.5)
                    .render(graphics);
            RenderSystem.disableDepthTest();
        }

        ms.popPose();
    }
}
