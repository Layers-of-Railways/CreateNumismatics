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

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.widget.Label;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class ScrollingLabel extends Label {

    protected int ticks = 0;

    protected final boolean languageLTR;

    public ScrollingLabel(int x, int y, Component text, int width) {
        super(x, y, text);

        this.width = width;
        this.languageLTR = TextUtils.isLeftToRight();
    }

    @Override
    public void tick() {
        super.tick();

        ticks += 1;
    }

    @Override
    protected void doRender(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (text == null || text.getString().isEmpty())
            return;

        RenderSystem.setShaderColor(1, 1, 1, 1);
        MutableComponent copy = text.plainCopy();
        if (suffix != null && !suffix.isEmpty())
            copy.append(suffix);

        int textWidth = font.width(copy);
        if (textWidth <= getWidth()) {
            graphics.drawString(font, copy, getX(), getY(), color, hasShadow);
        } else {
            String raw = copy.getString() + " ".repeat(10);
            int offset = ((languageLTR ? ticks : -ticks) / 2) % (raw.length());

            String substring = (raw + raw).substring(offset, offset + raw.length());
            // shrink until it fits in the right width
            while (font.width(substring) > getWidth()) {
                substring = substring.substring(0, substring.length() - 1);
            }
            graphics.drawString(font, substring, getX(), getY(), color, hasShadow);
        }
    }
}
