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

package dev.ithundxr.createnumismatics.content.salepoint;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import dev.ithundxr.createnumismatics.config.NumismaticsConfig;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceConfigurationPacket;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.ScrollSlotPacket;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SalepointConfigScreen extends AbstractSimiContainerScreen<SalepointConfigMenu> {

    private IconButton trustListButton;
    private IconButton confirmButton;

    private static final NumismaticsGuiTextures background = NumismaticsGuiTextures.SALEPOINT_CONFIG;
    private final ItemStack renderedItem = NumismaticsBlocks.SALEPOINT.asStack();

    private final int COIN_COUNT = Coin.values().length;

    private final Label[] coinLabels = new Label[COIN_COUNT];
    private final ScrollInput[] coinScrollInputs = new ScrollInput[COIN_COUNT];

    private List<Rect2i> extraAreas = Collections.emptyList();

    public SalepointConfigScreen(SalepointConfigMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    private @Nullable ISalepointState<?> getSalepointState() {
        return getMenu().getSalepointState();
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height + 2 + AllGuiTextures.PLAYER_INVENTORY.height);
        setWindowOffset(-20, 0);
        super.init();

        int x = leftPos;
        int y = topPos;

        trustListButton = new IconButton(x + 16, y + background.height - 24, AllIcons.I_VIEW_SCHEDULE);
        trustListButton.withCallback(() -> {
            menu.contentHolder.openTrustList();
        });
        addRenderableWidget(trustListButton);

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        for (Coin coin : Coin.values()) {
            final int i = coin.ordinal();

            final int baseX = x + 38 + (i < 3 ? 0 : 86 + 52);

            final int yIncrement = 22;
            final int baseY = y + 45 + (yIncrement * (i%3));

            coinLabels[i] = new Label(baseX + 18, baseY + 5, Components.immutableEmpty()).withShadow();
            addRenderableWidget(coinLabels[i]);

            coinScrollInputs[i] = new ScrollInput(baseX, baseY, 36, 18)
                .withRange(0, 129)
                .writingTo(coinLabels[i])
                .titled(Components.literal(TextUtils.titleCaseConversion(coin.getName(0))))
                .calling((value) -> {
                    menu.contentHolder.setPrice(coin, value);
                    coinLabels[i].setX(baseX + 18 - font.width(coinLabels[i].text) / 2);
                });
            addRenderableWidget(coinScrollInputs[i]);

            coinScrollInputs[i].setState(menu.contentHolder.getPrice(coin));
            coinScrollInputs[i].onChanged();
        }

        ISalepointState<?> salepointState = getSalepointState();
        if (salepointState != null)
            salepointState.createConfigWidgets(this::addRenderableWidget);

        extraAreas = ImmutableList.of(new Rect2i(x + background.width, y + background.height - 68, 84, 84));
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

        GuiGameElement.of(renderedItem).<GuiGameElement.GuiRenderBuilder>at(x + background.width + 6, y + background.height - 70, -200)
            .scale(5)
            .render(graphics);

        graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

        Couple<Integer> referenceAndSpurs = NumismaticsConfig.common().referenceCoin.get().convert(menu.contentHolder.getTotalPrice());
        int reference = referenceAndSpurs.getFirst();
        int spurs = referenceAndSpurs.getSecond();
        Component balanceLabel = Components.translatable("gui.numismatics.salepoint.price",
            TextUtils.formatInt(reference), NumismaticsConfig.common().referenceCoin.get().getName(reference), spurs);
        graphics.drawCenteredString(font, balanceLabel, x + (background.width - 8) / 2, y + 21, 0xFFFFFF);

        ISalepointState<?> salepointState = getSalepointState();
        if (salepointState != null)
            salepointState.renderConfigBackground(graphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        ISalepointState<?> salepointState = getSalepointState();
        if (salepointState != null)
            salepointState.renderConfigForeground(graphics, partialTicks, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        // todo tooltip for filter slot maybe
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ISalepointState<?> salepointState = getSalepointState();
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()
            && this.hoveredSlot.index == SalepointConfigMenu.FILTER_SLOT_INDEX && salepointState != null
            && salepointState.configGuiHasFilterSlot()) {
            NumismaticsPackets.PACKETS.send(new ScrollSlotPacket(this.hoveredSlot.index, delta, AllKeys.shiftDown()));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        NumismaticsPackets.PACKETS.send(new SliderStylePriceConfigurationPacket(menu.contentHolder));
        super.removed();
    }
}
