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

package dev.ithundxr.createnumismatics.content.backend.trust_list;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TrustListScreen extends AbstractSimiContainerScreen<TrustListMenu> {

    private IconButton confirmButton;

    private NumismaticsGuiTextures background = NumismaticsGuiTextures.TRUST_LIST;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public TrustListScreen(TrustListMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height + 2 + AllGuiTextures.PLAYER_INVENTORY.height);
        setWindowOffset(-20, 0);
        super.init();

        int x = leftPos;
        int y = topPos;

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            onClose();
        });
        addRenderableWidget(confirmButton);

        extraAreas = ImmutableList.of(new Rect2i(x + background.width, y + background.height - 64, 84, 74));
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.width);
        int invY = topPos + background.height + 2;
        renderPlayerInventory(graphics, invX, invY);

        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y);

        GuiGameElement.of(menu.renderedItem).<GuiGameElement
                .GuiRenderBuilder>at(x + background.width + 6, y + background.height - 70, -200)
            .scale(5)
            .render(graphics);

        graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);
    }
}
