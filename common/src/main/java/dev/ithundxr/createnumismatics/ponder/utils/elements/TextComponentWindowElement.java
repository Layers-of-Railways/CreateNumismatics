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

package dev.ithundxr.createnumismatics.ponder.utils.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.element.BoxElement;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderScene.SceneTransform;
import com.simibubi.create.foundation.ponder.element.AnimatedOverlayElement;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import com.simibubi.create.foundation.utility.Color;
import dev.ithundxr.createnumismatics.ponder.utils.PonderConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TextComponentWindowElement extends AnimatedOverlayElement {
    List<Component> bakedComponents;
    @Nullable ItemStack bakedItem;

    Consumer<List<Component>> textFiller;
    @Nullable Supplier<ItemStack> itemFiller;

    // from 0 to 200
    int y;

    Vec3 vec;

    boolean tickText;
    boolean tickItem;
    boolean nearScene = false;
    int color = PonderPalette.WHITE.getColor();
    boolean preserveTextColor = false;

    public class Builder {
        private final PonderScene scene;

        public Builder(PonderScene scene) {
            this.scene = scene;
        }

        public Builder colored(PonderPalette color) {
            TextComponentWindowElement.this.color = color.getColor();
            return this;
        }

        public Builder pointAt(Vec3 vec) {
            TextComponentWindowElement.this.vec = vec;
            return this;
        }

        public Builder independent(int y) {
            TextComponentWindowElement.this.y = y;
            return this;
        }

        public Builder independent() {
            return independent(0);
        }

        public Builder text(List<Component> components) {
            return text(c -> c.addAll(components));
        }

        public Builder text(Consumer<List<Component>> textFiller) {
            return text(textFiller, false);
        }

        public Builder text(BiConsumer<PonderScene, List<Component>> textFiller) {
            return text(textFiller, false);
        }

        public Builder text(BiConsumer<PonderScene, List<Component>> textFiller, boolean shouldUpdateOnTick) {
            return text((l) -> textFiller.accept(scene, l), shouldUpdateOnTick);
        }

        public Builder text(Consumer<List<Component>> textFiller, boolean shouldUpdateOnTick) {
            TextComponentWindowElement.this.textFiller = textFiller;
            TextComponentWindowElement.this.tickText = shouldUpdateOnTick;
            return this;
        }

        public Builder preserveTextColor() {
            TextComponentWindowElement.this.preserveTextColor = true;
            return this;
        }

        public Builder item(ItemStack item) {
            return item(() -> item);
        }

        public Builder item(Supplier<ItemStack> itemFiller) {
            return item(itemFiller, false);
        }

        public Builder item(Function<PonderScene, ItemStack> itemFiller) {
            return item(itemFiller, false);
        }

        public Builder item(Function<PonderScene, ItemStack> itemFiller, boolean shouldUpdateOnTick) {
            return item(() -> itemFiller.apply(scene), shouldUpdateOnTick);
        }

        public Builder item(Supplier<ItemStack> itemFiller, boolean shouldUpdateOnTick) {
            TextComponentWindowElement.this.itemFiller = itemFiller;
            TextComponentWindowElement.this.tickItem = shouldUpdateOnTick;
            return this;
        }

        public Builder placeNearTarget() {
            TextComponentWindowElement.this.nearScene = true;
            return this;
        }

        public Builder attachKeyFrame() {
            scene.builder()
                .addLazyKeyframe();
            return this;
        }
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);

        if (bakedComponents == null) {
            bakedComponents = new ArrayList<>();
            textFiller.accept(bakedComponents);
        } else if (tickText) {
            bakedComponents.clear();
            textFiller.accept(bakedComponents);
        }

        if (itemFiller != null && (bakedItem == null || tickItem)) {
            bakedItem = itemFiller.get();
        }
    }

    @Override
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    protected void render(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade) {
        if (fade < 1 / 16f)
            return;

        SceneTransform transform = scene.getTransform();
        Vec2 sceneToScreen = vec != null ? transform.sceneToScreen(vec, partialTicks)
            : new Vec2(screen.width / 2, (screen.height - 200) / 2 + y - 8);

        boolean settled = transform.xRotation.settled() && transform.yRotation.settled();
        float pY = settled ? (int) sceneToScreen.y : sceneToScreen.y;

        float yDiff = (screen.height / 2f - sceneToScreen.y - 10) / 100f;
        float targetX = (screen.width * Mth.lerp(yDiff * yDiff, 6f / 8, 5f / 8));

        if (nearScene)
            targetX = Math.min(targetX, sceneToScreen.x + 50);

        if (settled)
            targetX = (int) targetX;

        int textMaxWidth = (int) Math.min(screen.width - targetX, 180);
        Font font = screen.getFontRenderer();

        int titleLinesCount = 0;
        List<FormattedText> lines = new ArrayList<>();
        for (int i = 0; i < bakedComponents.size(); i++) {
            int[] count = {0};
            font.getSplitter().splitLines(bakedComponents.get(i), textMaxWidth, Style.EMPTY,
                (line, $) -> {
                    lines.add(line);
                    count[0]++; // MutableInt at home
                });
            if (i == 0 && bakedItem != null) {
                titleLinesCount = count[0];
            }
        }

        int boxWidth = 0;
        for (FormattedText line : lines) {
            boxWidth = Math.max(boxWidth, font.width(line));
        }

        int boxHeight = 8;
        if (lines.size() > 1) {
            boxHeight += (lines.size() - 1) * 10;
            if (lines.size() > titleLinesCount)
                boxHeight += 2; // gap between lines & next lines
        }

        float pY0 = pY;
        int safeHeight = 4 + PonderConstants.SAFE_Y_BUFFER;
        if (pY + boxHeight + safeHeight > screen.height) {
            pY = screen.height - boxHeight - safeHeight;
        }

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(0, pY, 400);

        new BoxElement().withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND_FLAT))
            .gradientBorder(Theme.p(Theme.Key.TEXT_WINDOW_BORDER))
            .at(targetX - 10, 3, 100)
            .withBounds(boxWidth, boxHeight - 1)
            .render(graphics);

        if (bakedItem != null) {
            GuiGameElement.of(bakedItem)
                .at(targetX - 12, 0, 450)
                .withAlpha(fade)
                .render(graphics);
        }

        int brighterColor = Color.mixColors(color, 0xFFffffdd, 1 / 2f);
        brighterColor = (0x00ffffff & brighterColor) | 0xff000000;
        if (vec != null) {
            ms.pushPose();
            ms.translate(sceneToScreen.x, pY0 - pY, 0);
            double lineTarget = (targetX - sceneToScreen.x) * fade;
            ms.scale((float) lineTarget, 1, 1);
            graphics.fillGradient(0, 0, 1, 1, -100, brighterColor, brighterColor);
            graphics.fillGradient(0, 1, 1, 2, -100, 0xFF494949, 0xFF393939);
            ms.popPose();
        }

        ms.translate(0, 0, 400);
        int tooltipY = 3;
        for (int i = 0; i < lines.size(); i++) {
            if (preserveTextColor) {
                graphics.drawString(font, Language.getInstance().getVisualOrder(lines.get(i)),
                    (int) (targetX - 10), tooltipY,
                    Color.WHITE.scaleAlpha(fade).getRGB(),
                    false);
            } else {
                graphics.drawString(font, lines.get(i).getString(),
                    (int) (targetX - 10), tooltipY,
                    new Color(brighterColor).scaleAlpha(fade).getRGB(),
                    false);
            }

            if (i + 1 == titleLinesCount)
                tooltipY += 2;

            tooltipY += 10;
        }

        ms.popPose();
    }

    public int getColor() {
        return color;
    }
}
