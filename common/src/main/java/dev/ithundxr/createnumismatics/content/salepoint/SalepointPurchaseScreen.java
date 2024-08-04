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
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import dev.ithundxr.createnumismatics.base.client.rendering.UIRenderHelper;
import dev.ithundxr.createnumismatics.config.NumismaticsConfig;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.registry.NumismaticsIcons;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.SalepointPurchasePacket;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SalepointPurchaseScreen extends AbstractSimiContainerScreen<SalepointPurchaseMenu> {

    private final ItemStack renderedItem = NumismaticsBlocks.SALEPOINT.asStack();
    private static final NumismaticsGuiTextures background = NumismaticsGuiTextures.SALEPOINT_PURCHASE;

    private Action action = Action.GO;
    private IconButton actionButton;
    private IconButton confirmButton;

    private Label countLabel;
    private ScrollInput countScrollInput;

    private int completedCooldown = 0;
    private int oldMultiplier = 0;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public SalepointPurchaseScreen(SalepointPurchaseMenu container, Inventory inv, Component title) {
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

        actionButton = new IconButton(x + 31, y + background.height - 24, NumismaticsIcons.I_SALE_GO);
        actionButton.withCallback(this::onAction);
        addRenderableWidget(actionButton);

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        countLabel = new Label(x + 104 + 3, y + 81 + 5, Components.immutableEmpty()).withShadow();
        addRenderableWidget(countLabel);

        countScrollInput = new ScrollInput(x + 104, y + 81, 45, 18);
        countScrollInput.withRange(1, 65);
        countScrollInput.writingTo(countLabel);
        countScrollInput.titled(Components.translatable("gui.numismatics.salepoint.count"));
        addRenderableWidget(countScrollInput);

        countScrollInput.setState(1);
        countScrollInput.onChanged();

        ISalepointState<?> salepointState = getSalepointState();
        if (salepointState != null)
            salepointState.createPurchaseWidgets(this::addRenderableWidget);

        extraAreas = ImmutableList.of(new Rect2i(x + background.width, y + background.height - 68, 84, 84));
        updateAction();
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

        Coin referenceCoin = NumismaticsConfig.common().referenceCoin.get();
        Couple<Integer> referenceAndSpurs = referenceCoin.convert(menu.contentHolder.getTotalPrice());
        int reference = referenceAndSpurs.getFirst();
        int spurs = referenceAndSpurs.getSecond();
        Component balanceLabel = Components.translatable("gui.numismatics.salepoint.price",
            TextUtils.formatInt(reference), referenceCoin.getName(reference), spurs);
        graphics.drawCenteredString(font, balanceLabel, x + (background.width - 8) / 2, y + 21, 0xFFFFFF);

        ISalepointState<?> salepointState = getSalepointState();
        if (salepointState != null)
            salepointState.renderPurchaseBackground(graphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        int x = leftPos;
        int y = topPos;

        NumismaticsGuiTextures progressTexture = NumismaticsGuiTextures.SALEPOINT_PURCHASE_PROGRESS;
        int target = menu.contentHolder.clientsideMultiplier;
        int progress = menu.contentHolder.clientsideProgress;

        if (target == 0 && completedCooldown > 0) {
            target = progress = oldMultiplier;
        }

        if (target > 0) {
            int width = progressTexture.width * progress / target;
            UIRenderHelper.drawCropped(
                graphics,
                x+53, y+background.height-24,
                width, 18,
                0,
                progressTexture
            );
            graphics.drawCenteredString(font, TextUtils.formatInt(progress) + " / " + TextUtils.formatInt(target),
                x + 53 + progressTexture.width/2, y + 119, 0xFFFFFF);
        }

        int totalPrice = menu.contentHolder.getTotalPrice() * countScrollInput.getState();
        Coin referenceCoin = NumismaticsConfig.common().referenceCoin.get();
        Couple<Integer> referenceAndSpurs = referenceCoin.convert(totalPrice);
        int reference = referenceAndSpurs.getFirst();
        int spurs = referenceAndSpurs.getSecond();

        referenceCoin.getIcon().render(graphics, x+133, y+45);
        Coin.SPUR.getIcon().render(graphics, x+133, y+63);

        for (boolean spur : Iterate.falseAndTrue) {
            FormattedCharSequence seq = Components.literal(TextUtils.formatInt(spur ? spurs : reference))
                .getVisualOrderText();

            int width = font.width(seq);
            int textX = x + 133 - width;
            graphics.drawString(font, seq, textX, y + (spur ? 63 : 45) + 4, 0x202020, false);
        }

        ISalepointState<?> salepointState = getSalepointState();
        if (salepointState != null)
            salepointState.renderPurchaseForeground(graphics, partialTicks, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        // todo tooltip for filter slot maybe
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateAction();

        if (completedCooldown > 0)
            completedCooldown--;

        if (menu.contentHolder.justCompletedMultiplier != null) {
            completedCooldown = 20 * 3;
            oldMultiplier = menu.contentHolder.justCompletedMultiplier;
            menu.contentHolder.justCompletedMultiplier = null;
        }
    }

    private void onAction() {
        completedCooldown = 0;
        switch (action) {
            case GO -> NumismaticsPackets.PACKETS.send(new SalepointPurchasePacket(countScrollInput.getState()));
            case CANCEL -> NumismaticsPackets.PACKETS.send(new SalepointPurchasePacket(0));
            case ALERT -> {
                if (menu.contentHolder.clientsideMultiplier > 0)
                    NumismaticsPackets.PACKETS.send(new SalepointPurchasePacket(0));
            }
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void updateAction() {
        action = Action.GO;
        Component alert = Components.translatable("gui.numismatics.salepoint.go");

        if (menu.serverSentStateMessage != null) {
            action = Action.ALERT;
            alert = menu.serverSentStateMessage;
        }

        if (action != Action.ALERT) {
            ItemStack card = menu.getCard();
            if (card.isEmpty()) {
                action = Action.ALERT;
                alert = Components.translatable("gui.numismatics.salepoint.no_card");
            } else {
                if (menu.serverSentCardMessage != null) {
                    action = Action.ALERT;
                    alert = menu.serverSentCardMessage;
                } else if (menu.serverSentMaxWithdrawal < menu.contentHolder.getTotalPrice() * countScrollInput.getState()) {
                    action = Action.ALERT;
                    alert = Components.translatable("gui.numismatics.vendor.insufficient_funds");
                }
            }
        }

        if (action == Action.GO) {
            if (menu.contentHolder.clientsideMultiplier > 0) {
                action = Action.CANCEL;
                alert = Components.translatable("gui.numismatics.salepoint.cancel");
            }
        }

        actionButton.setIcon(action.icon);
        actionButton.setToolTip(alert);
        if (action == Action.ALERT && menu.contentHolder.clientsideMultiplier > 0)
            actionButton.getToolTip().add(Components.translatable("gui.numismatics.salepoint.cancel"));
    }

    private enum Action {
        GO(NumismaticsIcons.I_SALE_GO),
        CANCEL(NumismaticsIcons.I_SALE_CANCEL),
        ALERT(NumismaticsIcons.I_SALE_ALERT)
        ;

        private final NumismaticsIcons icon;

        Action(NumismaticsIcons icon) {
            this.icon = icon;
        }
    }
}
