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

package dev.ithundxr.createnumismatics.content.bank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsScreen;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.base.client.rendering.UIRenderHelper;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.minecraft.client.gui.screens.Screen.hasShiftDown;

public class TripleCoinSliderWidget extends AbstractSimiWidget {

    private static final Coin[] ROWS = new Coin[] {
        Coin.SPUR,
        Coin.COG,
        Coin.SUN
    };
    private static final int milestoneSize = 4;

    protected final Font font;
    protected final int maxLabelWidth;
    protected final int valueBarWidth;
    protected final int[] values = new int[] {0, 0, 0};

    protected @Nullable Integer draggingRow = null;
    protected int soundCooldown = 0;
    protected @Nullable EditHandler onEdit;
    protected boolean maxIsInfinite = false;

    protected TripleCoinSliderWidget(Font font, int x, int y) {
        super(x, y, 48, 16);
        this.font = font;

        int maxValue = getMaxValue();
        int milestoneCount = maxValue / getMilestoneInterval() + 1;
        int scale = maxValue > 128 ? 1 : 2;

        maxLabelWidth = Arrays.stream(ROWS)
            .map(Coin::getTranslatedName)
            .map(TextUtils::titleCaseConversion)
            .mapToInt(font::width)
            .max()
            .orElse(0) + 4;

        valueBarWidth = (maxValue + 1) * scale + 1 + milestoneCount * milestoneSize;
        this.width = (maxLabelWidth + 14) + (valueBarWidth + 10);
        this.height = (ROWS.length * 15) + 1;
    }

    @Contract("_ -> this")
    protected TripleCoinSliderWidget withEditCallback(EditHandler onEdit) {
        this.onEdit = onEdit;
        return this;
    }

    @Contract("_ -> this")
    protected TripleCoinSliderWidget withValue(int spur) {
        Couple<Integer> sunAndExtra = Coin.SUN.convert(spur, 64);
        Couple<Integer> cogAndExtra = Coin.COG.convert(sunAndExtra.getSecond(), 64);
        return withValues(
            cogAndExtra.getSecond(), // Spurs
            cogAndExtra.getFirst(),  // Cogs
            sunAndExtra.getFirst()   // Suns
        );
    }

    @Contract("_, _, _ -> this")
    protected TripleCoinSliderWidget withValues(int spur, int cog, int sun) {
        values[0] = spur;
        values[1] = cog;
        values[2] = sun;
        return this;
    }

    @Contract("_ -> this")
    protected TripleCoinSliderWidget setMaxIsInfinite(boolean maxIsInfinite) {
        this.maxIsInfinite = maxIsInfinite;
        return this;
    }

    protected int getMaxValue() {
        return 64;
    }

    protected int getMilestoneInterval() {
        return 16;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public Vec2 getCoordinateOfValue(int row, int column) {
        int scale = getMaxValue() > 128 ? 1 : 2;

        float xOut = getX()
            + ((Math.max(1, column) - 1) / getMilestoneInterval()) * milestoneSize
            + column * scale
            + 1.5f;
        xOut += maxLabelWidth + 14 + 4 + 3;

        if (column % getMilestoneInterval() == 0)
            xOut += milestoneSize / 2;
        if (column > 0)
            xOut += milestoneSize;

        float yOut = getY() + (row + .5f) * 15 - .5f;
        return new Vec2(xOut, yOut);
    }

    protected boolean canStartDragging(int mouseX, int mouseY) {
        int x = getX();
        int y = getY();
        int relMouseX = mouseX - x;
        int relMouseY = mouseY - y;

        return maxLabelWidth + 14 + 1 <= relMouseX && relMouseX < width + 4
            && 0 <= relMouseY && relMouseY < height;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        if (!this.active || !this.visible)
            return false;

        return mouseX >= (double)this.getX()
            && mouseY >= (double)this.getY()
            && mouseX < (double)(this.getX() + this.width + 4)
            && mouseY < (double)(this.getY() + this.height);
    }

    protected int getClosestRow(int mouseY) {
        int y = getY();
        int relMouseY = mouseY - y;

        return Math.min(ROWS.length - 1, Math.max(0, relMouseY / 15));
    }

    protected int getClosestColumn(int mouseX, int row) {
        int column = 0;
        boolean milestonesOnly = hasShiftDown();

        double bestDiff = Double.MAX_VALUE;
        for (; column <= getMaxValue(); column++) {
            Vec2 coord = getCoordinateOfValue(row, milestonesOnly ? column * getMilestoneInterval() : column);
            double diff = Math.abs(coord.x - mouseX);
            if (bestDiff < diff)
                break;
            bestDiff = diff;
        }
        column -= 1;

        return milestonesOnly ? Math.min(column * getMilestoneInterval(), getMaxValue()) : column;
    }

    @Override
    protected void doRender(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getX();
        int y = getY();
        int milestoneCount = getMaxValue() / getMilestoneInterval() + 1;
        int scale = getMaxValue() > 128 ? 1 : 2;

        /*============*/
        /* Background */
        /*============*/
        int a = 0xc0 << 24;
        graphics.fillGradient(x + maxLabelWidth + 14 + 3, y + 1, x + this.width - 1, y + this.height - 1, 0x101010 | a, 0x101010 | a);

        /*================*/
        /* Main Rendering */
        /*================*/
        int zLevel = 0;

        renderBrassFrame(graphics, x + maxLabelWidth + 14 + 3, y - 3 + 3, valueBarWidth + 8, ROWS.length * 15 + 1);

        int trackY = y + 3;
        int labelY = trackY;
        for (Coin coin : ROWS) {
            String displayName = TextUtils.titleCaseConversion(coin.getTranslatedName());
            int valueBarX = x + maxLabelWidth + 14 + 4 + 3;

            UIRenderHelper.drawCropped(graphics, x - 4 + 7, labelY - 1, maxLabelWidth + 8, 15,
                zLevel, NumismaticsGuiTextures.VALUE_SETTINGS_LABEL_BG_TALL);

            PoseStack ms = graphics.pose();
            ms.pushPose();
            ms.translate(x + maxLabelWidth + 2, labelY + 2, zLevel);
            ms.scale(8.0f, 8.0f, 8.0f);
            coin.getIcon().render(ms, graphics.bufferSource(), 0xffffff);
            ms.popPose();

            for (int w = 0; w < valueBarWidth; w += AllGuiTextures.VALUE_SETTINGS_BAR.width - 1)
                UIRenderHelper.drawCropped(graphics, valueBarX + w, trackY + 1,
                    Math.min(AllGuiTextures.VALUE_SETTINGS_BAR.width - 1, valueBarWidth - w), 8,
                    zLevel, AllGuiTextures.VALUE_SETTINGS_BAR);
            graphics.drawString(font, displayName, x + 7, labelY + 2, 0x442000, false);

            int milestoneX = valueBarX;
            for (int milestone = 0; milestone < milestoneCount; milestone++) {
                AllGuiTextures.VALUE_SETTINGS_MILESTONE.render(graphics, milestoneX, trackY + 1);
                milestoneX += milestoneSize + getMilestoneInterval() * scale;
            }

            trackY += 15;
            labelY += 14;
        }
        renderBrassFrame(graphics, x, y, maxLabelWidth + 14, ROWS.length * 15 + 1);

        if (draggingRow != null) {
            int closestColumn = getClosestColumn(mouseX, draggingRow);

            int lastValue = values[draggingRow];
            if (closestColumn != lastValue) {
                values[draggingRow] = closestColumn;
                if (onEdit != null)
                    onEdit.apply(values[0], values[1], values[2]);
                if (soundCooldown == 0) {
                    float pitch = closestColumn / (float) getMaxValue();
                    pitch = Mth.lerp(pitch, 1.15f, 1.5f);
                    Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(AllSoundEvents.SCROLL_VALUE.getMainEvent(), pitch, 0.25f));
                    ScrollValueHandler.wrenchCog.bump(3, -(closestColumn - lastValue) * 10);
                }
            }
        }

        boolean infinite = maxIsInfinite && values[0] == 64 && values[1] == 64 && values[2] == 64;
        for (int row = 0; row < values.length; row++) {
            int value = values[row];
            Vec2 coordinate = getCoordinateOfValue(row, value);
            // 221E = infinity symbol
            Component cursorText = infinite ? Components.literal("\u221E") : Lang.number(value).component();

            int cursorWidth = (font.width(cursorText) / 2) * 2 + 3;
            int cursorX = ((int) (coordinate.x)) - cursorWidth / 2;
            int cursorY = ((int) (coordinate.y)) - 6;

            AllGuiTextures.VALUE_SETTINGS_CURSOR_LEFT.render(graphics, cursorX - 3, cursorY);
            UIRenderHelper.drawCropped(graphics, cursorX, cursorY, cursorWidth, 14,
                zLevel, AllGuiTextures.VALUE_SETTINGS_CURSOR);
            AllGuiTextures.VALUE_SETTINGS_CURSOR_RIGHT.render(graphics, cursorX + cursorWidth, cursorY);

            graphics.drawString(font, cursorText, cursorX + 2, cursorY + 3, (draggingRow != null && row == draggingRow) ? 0x703a0a : 0x442000, false);
        }
    }

    /** Copied from {@link ValueSettingsScreen} */
    protected void renderBrassFrame(GuiGraphics graphics, int x, int y, int w, int h) {
        AllGuiTextures.BRASS_FRAME_TL.render(graphics, x, y);
        AllGuiTextures.BRASS_FRAME_TR.render(graphics, x + w - 4, y);
        AllGuiTextures.BRASS_FRAME_BL.render(graphics, x, y + h - 4);
        AllGuiTextures.BRASS_FRAME_BR.render(graphics, x + w - 4, y + h - 4);
        int zLevel = 0;

        if (h > 8) {
            UIRenderHelper.drawStretched(graphics, x, y + 4, 3, h - 8, zLevel, AllGuiTextures.BRASS_FRAME_LEFT);
            UIRenderHelper.drawStretched(graphics, x + w - 3, y + 4, 3, h - 8, zLevel, AllGuiTextures.BRASS_FRAME_RIGHT);
        }

        if (w > 8) {
            UIRenderHelper.drawCropped(graphics, x + 4, y, w - 8, 3, zLevel, AllGuiTextures.BRASS_FRAME_TOP);
            UIRenderHelper.drawCropped(graphics, x + 4, y + h - 3, w - 8, 3, zLevel, AllGuiTextures.BRASS_FRAME_BOTTOM);
        }
    }

    @Override
    public void tick() {
        if (soundCooldown > 0)
            soundCooldown--;

        super.tick();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        if (!canStartDragging((int) mouseX, (int) mouseY))
            return;

        draggingRow = getClosestRow((int) mouseY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        draggingRow = null;
    }

    @FunctionalInterface
    public interface EditHandler {
        void apply(int spurs, int cogs, int suns);
    }
}
