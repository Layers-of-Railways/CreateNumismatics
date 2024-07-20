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

package dev.ithundxr.createnumismatics.content.bank;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.AuthorizationType;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.Limit;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.SubAccount;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.sub_account.OpenSubAccountEditScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.*;
import java.util.function.Consumer;

public class SubAccountListScreen extends AbstractSimiContainerScreen<SubAccountListMenu> {
    private static final int CARD_HEADER = 22;
    private static final int CARD_BODY = 66;
    private static final int CARD_WIDTH = 208;
    private static final int CARD_SPACING = 2;
    private static final int UNLIMITED_VALUE = Coin.SUN.toSpurs(64) + Coin.COG.toSpurs(64) + 64;

    private static final Color REMOVE_OFF_COLOR = new Color(0x606060, false).setImmutable();
    private static final Color REMOVE_ON_COLOR = new Color(0xff0029, false).setImmutable();

    private final NumismaticsGuiTextures background = NumismaticsGuiTextures.SUB_ACCOUNT_LIST;
    private final ItemStack renderedItem = NumismaticsBlocks.BANK_TERMINAL.asStack();

    private List<Rect2i> extraAreas = Collections.emptyList();

    private final LerpedFloat scroll = LerpedFloat.linear()
        .startWithValue(0);

    private final LerpedFloat removeProgress = LerpedFloat.linear()
        .startWithValue(0);
    private UUID removeTarget = null;

    private IconButton confirmButton;
    private IconButton addButton;
    private EditBox nameBox;

    private boolean hasPopup = false;

    private IconButton editorConfirm;
    private EditBox editorLabelBox;

    private final Map<UUID, TripleCoinSliderWidget> sliders = new HashMap<>();


    public SubAccountListScreen(SubAccountListMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height);
        super.init();
        clearWidgets();

        int x = leftPos;
        int y = topPos;

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(menu.contentHolder::openNormalMenu);
        addRenderableWidget(confirmButton);

        addButton = new IconButton(x + 173, y + background.height - 24, AllIcons.I_ADD);
        addButton.withCallback(() -> {
            String label = nameBox.getValue();
            if (label.isBlank())
                label = "Sub Account";
            menu.addSubAccount(label);
            nameBox.setValue("");
        });
        addRenderableWidget(addButton);

        nameBox = new EditBox(this.font, x + 12+1, y + 197+4, 152, 16, Components.translatable("gui.numismatics.bank_terminal.sub_accounts.name_box"));
        nameBox.setBordered(false);
        nameBox.setMaxLength(25);
        nameBox.setValue("");
        addRenderableWidget(nameBox);

        extraAreas = ImmutableList.of(new Rect2i(x + background.width, y + background.height - 64, 84, 74));
        stopPopup();
    }

    protected void editSubAccount(UUID subAccountID) {
        menu.openSubAccountEditScreen(subAccountID);
        NumismaticsPackets.PACKETS.send(new OpenSubAccountEditScreenPacket(subAccountID));
    }

    protected void startPopup() {
        confirmButton.visible = false;
        addButton.visible = false;
        nameBox.visible = false;
        nameBox.setFocused(false);

        if (hasPopup)
            return;

        hasPopup = true;

        editorConfirm = new IconButton(leftPos + 56 + 168 - 13, topPos + 65 + 22, AllIcons.I_CONFIRM);
        addRenderableWidget(editorConfirm);

        int x = leftPos - 12;
        int y = topPos - 5;
        Label authorizationTypeLabel = new ScrollingLabel(x + 67 + 3, y + 89 + 5, Components.immutableEmpty(), 108 - 6).withShadow();

        SelectionScrollInput authorizationTypeScroll = new SelectionScrollInput(x + 67, y + 89, 108, 18);
        authorizationTypeScroll.forOptions(AuthorizationType.labeledComponents());
        authorizationTypeScroll.writingTo(authorizationTypeLabel);
        authorizationTypeScroll.titled(Components.translatable("gui.numismatics.bank_terminal.sub_accounts.authorization_type"));
        authorizationTypeScroll.calling(idx -> {
            SubAccountListMenu.OpenSubAccountInformation osa = menu.getOpenSubAccount();
            if (osa == null)
                return;

            menu.setAuthorizationType(osa.authorizationID, AuthorizationType.values()[idx]);
        });

        SubAccountListMenu.OpenSubAccountInformation osa = menu.getOpenSubAccount();
        SubAccount subAccount = null;
        if (osa != null) {
            subAccount = menu.contentHolder.getSubAccountNoAuth(osa.authorizationID);
            if (subAccount != null) {
                authorizationTypeScroll.setState(subAccount.getAuthorizationType().ordinal());
                authorizationTypeScroll.onChanged();
            }
        }

        addRenderableWidget(authorizationTypeScroll);
        addRenderableWidget(authorizationTypeLabel);

        String existingLabel = subAccount == null ? null : subAccount.getLabel();
        if (existingLabel == null || existingLabel.isBlank()) {
            existingLabel = "Sub Account";
        }

        Consumer<String> onTextChanged = s -> editorLabelBox.setX(nameBoxX(s, editorLabelBox));
        editorLabelBox = new EditBox(new NoShadowFontWrapper(font), x + 23, y + 4, background.width - 20, 10,
            Components.literal(existingLabel));
        editorLabelBox.setBordered(false);
        editorLabelBox.setMaxLength(25);
        editorLabelBox.setTextColor(0x592424);
        editorLabelBox.setValue(existingLabel);
        editorLabelBox.setFocused(false);
        editorLabelBox.mouseClicked(0, 0, 0);
        editorLabelBox.setResponder(onTextChanged);
        editorLabelBox.setX(nameBoxX(editorLabelBox.getValue(), editorLabelBox));
        addRenderableWidget(editorLabelBox);

        //popupWidgets = ImmutableList.of(authorizationTypeLabel, authorizationTypeScroll);
    }

    protected void stopPopup() {
        confirmButton.visible = true;
        addButton.visible = true;
        nameBox.visible = true;

        if (!hasPopup)
            return;

        sliders.clear();
        removeWidget(editorConfirm);
        editorConfirm = null;
        syncName();

        hasPopup = false;
        init();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        scroll.tickChaser();
        removeProgress.tickChaser();

        if (editorLabelBox != null && getFocused() != editorLabelBox) {
            editorLabelBox.setCursorPosition(editorLabelBox.getValue().length());
            editorLabelBox.setHighlightPos(editorLabelBox.getCursorPosition());
        }

        nameBox.tick();
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = minecraft.getFrameTime();

        if (menu.slotsActive()) {
            if (!hasPopup) {
                startPopup();
            }
            super.render(graphics, mouseX, mouseY, partialTicks);
        } else {
            if (hasPopup) {
                stopPopup();
            }
            renderBackground(graphics);
            renderBg(graphics, partialTicks, mouseX, mouseY);
            for (Renderable widget : renderables)
                widget.render(graphics, mouseX, mouseY, partialTicks);
            renderForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    protected void renderSubAccounts(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack ms = graphics.pose();
        UIRenderHelper.swapAndBlitColor(minecraft.getMainRenderTarget(), UIRenderHelper.framebuffer);

        int yOffset = 25;
        float scrollOffset = -scroll.getValue(partialTicks);

        if (menu.contentHolder.hasSubAccounts()) {
            for (SubAccount subAccount : menu.contentHolder.getAlphabetizedSubAccounts()) {
                startStencil(graphics, leftPos + 3, topPos + 16, 220, 173);
                ms.pushPose();
                ms.translate(0, scrollOffset, 0);

                int cardHeight = renderSubAccount(graphics, subAccount, yOffset, mouseX, mouseY, partialTicks);
                yOffset += cardHeight + CARD_SPACING;

                ms.popPose();
                endStencil();
            }
        } else {
            startStencil(graphics, leftPos + 3, topPos + 16, 220, 173);
            ms.pushPose();
            ms.translate(0, scrollOffset, 0);

            renderSubAccountHelp(graphics, yOffset, mouseX, mouseY, partialTicks);

            ms.popPose();
            endStencil();
        }

        int zLevel = 200;
        graphics.fillGradient(leftPos + 3, topPos + 16, leftPos + 3 + 220, topPos + 16 + 10, zLevel, 0x77000000,
            0x00000000);
        graphics.fillGradient(leftPos + 3, topPos + 179, leftPos + 3 + 220, topPos + 179 + 10, zLevel, 0x00000000,
            0x77000000);
        UIRenderHelper.swapAndBlitColor(UIRenderHelper.framebuffer, minecraft.getMainRenderTarget());
    }

    protected TripleCoinSliderWidget getOrCreateWidgetForSubAccount(SubAccount subAccount) {
        Integer limit = subAccount.getTotalLimit().getLimit();
        return sliders.computeIfAbsent(
            subAccount.getAuthorizationID(),
            $ -> new TripleCoinSliderWidget(font, 2, CARD_HEADER + 18)
                .setMaxIsInfinite(true)
                .withValue(limit == null ? UNLIMITED_VALUE : limit)
                .withEditCallback((int spurs, int cogs, int suns) -> {
                    int total = Coin.SUN.toSpurs(suns) + Coin.COG.toSpurs(cogs) + spurs;
                    if (total == UNLIMITED_VALUE) {
                        menu.setLimit(subAccount.getAuthorizationID(), null);
                    } else {
                        menu.setLimit(subAccount.getAuthorizationID(), total);
                    }
                })
        );
    }

    protected int renderSubAccount(GuiGraphics graphics, SubAccount subAccount, int yOffset, int mouseX, int mouseY,
                                   float partialTicks) {
        int zLevel = -100;

        AllGuiTextures light = AllGuiTextures.SCHEDULE_CARD_LIGHT;
        AllGuiTextures medium = AllGuiTextures.SCHEDULE_CARD_MEDIUM;
        AllGuiTextures dark = AllGuiTextures.SCHEDULE_CARD_DARK;

        int cardWidth = CARD_WIDTH;
        int cardHeader = CARD_HEADER;
        int cardHeight = cardHeader + CARD_BODY;

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(leftPos + 9, topPos + yOffset, 0);

        UIRenderHelper.drawStretched(graphics, 0, 1, cardWidth, cardHeight - 2, zLevel, light);
        UIRenderHelper.drawStretched(graphics, 1, 0, cardWidth - 2, cardHeight, zLevel, light);
        UIRenderHelper.drawStretched(graphics, 1, 1, cardWidth - 2, cardHeight - 2, zLevel, dark);
        UIRenderHelper.drawStretched(graphics, 2, 2, cardWidth - 4, cardHeight - 4, zLevel, medium);
        UIRenderHelper.drawStretched(graphics, 2, 2, cardWidth - 4, cardHeader, zLevel, medium);
        UIRenderHelper.drawStretched(graphics, 8, cardHeader, cardWidth - 16, 1, zLevel, light);
        UIRenderHelper.drawStretched(graphics, 9, cardHeader + 1, cardWidth - 16, 1, zLevel, dark);

        boolean isTargeted = removeTarget != null && removeTarget.equals(subAccount.getAuthorizationID());
        NumismaticsGuiTextures.SUB_ACCOUNT_LIST_DELETE_COLORABLE.render(graphics, 2, 2,
            REMOVE_OFF_COLOR.mixWith(REMOVE_ON_COLOR, isTargeted ? removeProgress.getValue() : 0.0f));
        NumismaticsGuiTextures.SUB_ACCOUNT_LIST_EDIT.render(graphics, cardWidth - 14, 2);
        NumismaticsGuiTextures.SUB_ACCOUNT_LIST_RESET.render(graphics, cardWidth - 15, 26);

        // Render label
        graphics.drawCenteredString(font, subAccount.getLabel(), cardWidth / 2, 6, 0xFFFFFF);

        // Draw spending info
        Limit totalLimit = subAccount.getTotalLimit();
        {
            //graphics.drawCenteredString(font, totalLimit.describe(), cardWidth / 2, CARD_HEADER + 6, 0xAAAAAA);
            FormattedCharSequence formattedCharSequence = totalLimit.describe().getVisualOrderText();
            int width = font.width(formattedCharSequence);

            int x = (cardWidth / 2) - (width / 2);

            int max_x = cardWidth - 17;
            if (x + width > max_x) {
                x = max_x - width;
            }
            graphics.drawString(font, formattedCharSequence, x, CARD_HEADER + 6, 0xAAAAAA);
        }

        // Draw sliders
        TripleCoinSliderWidget widget = getOrCreateWidgetForSubAccount(subAccount);
        widget.render(graphics, mouseX - leftPos - 3 - 6, mouseY - topPos - 16 - 9, partialTicks);

        ms.popPose();

        return cardHeight;
    }

    protected void renderSubAccountHelp(GuiGraphics graphics, int yOffset, int mouseX, int mouseY, float partialTicks) {
        int zLevel = -100;

        AllGuiTextures light = AllGuiTextures.SCHEDULE_CARD_LIGHT;
        AllGuiTextures medium = AllGuiTextures.SCHEDULE_CARD_MEDIUM;
        AllGuiTextures dark = AllGuiTextures.SCHEDULE_CARD_DARK;

        int cardWidth = CARD_WIDTH;
        int cardHeader = CARD_HEADER;
        int cardHeight = cardHeader + CARD_BODY;

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(leftPos + 9, topPos + yOffset, 0);

        UIRenderHelper.drawStretched(graphics, 0, 1, cardWidth, cardHeight - 2, zLevel, light);
        UIRenderHelper.drawStretched(graphics, 1, 0, cardWidth - 2, cardHeight, zLevel, light);
        UIRenderHelper.drawStretched(graphics, 1, 1, cardWidth - 2, cardHeight - 2, zLevel, dark);
        UIRenderHelper.drawStretched(graphics, 2, 2, cardWidth - 4, cardHeight - 4, zLevel, medium);
        UIRenderHelper.drawStretched(graphics, 2, 2, cardWidth - 4, cardHeader, zLevel, medium);
        UIRenderHelper.drawStretched(graphics, 8, cardHeader, cardWidth - 16, 1, zLevel, light);
        UIRenderHelper.drawStretched(graphics, 9, cardHeader + 1, cardWidth - 16, 1, zLevel, dark);

        graphics.drawCenteredString(
            font, Components.translatable("gui.numismatics.bank_terminal.sub_accounts.help"),
            cardWidth / 2, 6,
            0xFFFFFF
        );

        for (int i = 0; i < 6; i++) {
            graphics.drawString(
                font, Components.translatable("gui.numismatics.bank_terminal.sub_accounts.help.line."+(i+1)),
                6, CARD_HEADER + 6 + (i * font.lineHeight),
                0xC0C0C0
            );
        }

        ms.popPose();
    }

    public boolean action(@Nullable GuiGraphics graphics, double mouseX, double mouseY, int click) {
        if (menu.slotsActive())
            return false;

        int mx = (int) mouseX;
        int my = (int) mouseY;
        int x = mx - leftPos - 3;
        int y = my - topPos - 16;

        //renderActionTooltip(graphics, ImmutableList.of(Components.literal("x: "+x), Components.literal("y: "+y)), mx, my);

        if (x < 0 || x >= 219)
            return false;
        if (y < 0 || y >= 172)
            return false;
        y += (int) scroll.getValue(0);

        int yOffset = 0;

        for (SubAccount subAccount : menu.contentHolder.getAlphabetizedSubAccounts()) {
            int cardHeight = CARD_HEADER + CARD_BODY;

            int cx = x - 6;
            int cy = y - 9 - yOffset;

            yOffset += cardHeight + CARD_SPACING;

            if (cx < 0 || cx >= CARD_WIDTH)
                continue;
            if (cy < 0 || cy >= cardHeight)
                continue;

            //renderActionTooltip(graphics, ImmutableList.of(Components.literal("cx: "+cx), Components.literal("cy: "+cy)), mx, my + 30);

            if (cy >= 4 && cy <= 12) {
                if (cx >= 3 && cx <= 11) {
                    boolean ready = removeProgress.settled() && removeProgress.getValue() > 0.9f
                        && removeTarget != null && removeTarget.equals(subAccount.getAuthorizationID());
                    if (ready) {
                        renderActionTooltip(graphics, ImmutableList.of(Components.translatable("gui.numismatics.bank_terminal.sub_accounts.remove.confirm")), mx, my);
                    } else {
                        renderActionTooltip(graphics, ImmutableList.of(Components.translatable("gui.numismatics.bank_terminal.sub_accounts.remove")), mx, my);
                    }
                    if (click == 0) {
                        if (ready) {
                            menu.removeSubAccount(subAccount.getAuthorizationID());
                            removeProgress.updateChaseTarget(0.0f);
                            removeProgress.setValue(0.0f);
                            removeTarget = null;
                        } else {
                            removeProgress.chase(1.0f, 0.4f, LerpedFloat.Chaser.LINEAR);
                            removeTarget = subAccount.getAuthorizationID();
                        }
                    }
                    return true;
                } else {
                    removeProgress.updateChaseTarget(0.0f);
                    removeProgress.setValue(0.0f);
                    removeTarget = null;
                }
                if (cx >= 195 && cx <= 203) {
                    renderActionTooltip(graphics, ImmutableList.of(Components.translatable("gui.numismatics.bank_terminal.sub_accounts.edit")), mx, my);
                    if (click == 0) {
                        editSubAccount(subAccount.getAuthorizationID());
                    }
                    return true;
                }
            } else {
                removeProgress.updateChaseTarget(0.0f);
                removeProgress.setValue(0.0f);
                removeTarget = null;
            }
            if (cy >= 26 && cy <= 37) {
                if (cx >= 193 && cx <= 204) {
                    renderActionTooltip(graphics, ImmutableList.of(Components.translatable("gui.numismatics.bank_terminal.sub_accounts.reset_spending")), mx, my);
                    if (click == 0) {
                        menu.resetSubAccountSpending(subAccount.getAuthorizationID());
                    }
                    return true;
                }
            }

            TripleCoinSliderWidget widget = getOrCreateWidgetForSubAccount(subAccount);
            if (widget.isMouseOver(cx, cy))
                renderActionTooltip(graphics, widget.getToolTip(), mx, my);
            if (widget.mouseClicked(cx, cy, click))
                return true;
        }

        return false;
    }

    private void renderActionTooltip(@Nullable GuiGraphics graphics, List<Component> tooltip, int mx, int my) {
        if (graphics != null)
            graphics.renderTooltip(font, tooltip, Optional.empty(), mx, my);
    }

    protected void startStencil(GuiGraphics graphics, float x, float y, float w, float h) {
        RenderSystem.clear(GL30.GL_STENCIL_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        GL11.glDisable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilMask(~0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0xFF);
        RenderSystem.stencilFunc(GL11.GL_NEVER, 1, 0xFF);

        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(x, y, 0);
        matrixStack.scale(w, h, 1);
        graphics.fillGradient(0, 0, 1, 1, -100, 0xff000000, 0xff000000);
        matrixStack.popPose();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
    }

    protected void endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (editorConfirm != null && editorConfirm.isMouseOver(pMouseX, pMouseY)) {
            editSubAccount(null);
            return true;
        }

        if (action(null, pMouseX, pMouseY, pButton))
            return true;

        int popupLeftPos = leftPos - 12;
        int popupTopPos = topPos - 5;
        if (hasPopup && !editorLabelBox.isFocused() && pMouseY > popupTopPos && pMouseY < popupTopPos + 14
            && pMouseX > popupLeftPos && pMouseX < popupLeftPos + NumismaticsGuiTextures.SUB_ACCOUNT_LIST_POPUP.width) {
            editorLabelBox.setFocused(true);
            editorLabelBox.setHighlightPos(0);
            setFocused(editorLabelBox);
            return true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private void syncName() {
        if (menu.getOpenSubAccount() != null && editorLabelBox != null) {
            if (editorLabelBox.getValue().isBlank())
                editorLabelBox.setValue("Sub Account");
            menu.setLabel(menu.getOpenSubAccount().authorizationID, editorLabelBox.getValue());
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean hitEnter = getFocused() instanceof EditBox
            && (pKeyCode == InputConstants.KEY_RETURN || pKeyCode == InputConstants.KEY_NUMPADENTER);

        if (hitEnter && editorLabelBox != null && editorLabelBox.isFocused()) {
            if (editorLabelBox.getValue().isBlank())
                editorLabelBox.setValue("Sub Account");
            editorLabelBox.setFocused(false);
            syncName();

            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        sliders.forEach((id, widget) -> widget.mouseReleased(mouseX, mouseY, button));

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        GuiGameElement.of(renderedItem).<GuiGameElement
                .GuiRenderBuilder>at(leftPos + background.width + 6, topPos + background.height - 64, -200)
            .scale(5)
            .render(graphics);
        action(graphics, mouseX, mouseY, -1);

        if (!hasPopup)
            return;

        int x = leftPos - 12;
        int y = topPos  - 5;

        SubAccountListMenu.OpenSubAccountInformation osa = menu.getOpenSubAccount();
        if (osa == null)
            return;
        SubAccount subAccount = menu.contentHolder.getSubAccountNoAuth(osa.authorizationID);
        if (subAccount == null)
            return;
        AuthorizationType authorizationType = subAccount.getAuthorizationType();
        authorizationType.icon.render(graphics, x + 43, y + 90);

        if (mouseX >= x + 42 && mouseY >= y + 89 && mouseX < x + 42 + 18 && mouseY < y + 89 + 18) {
            renderActionTooltip(graphics, ImmutableList.of(
                authorizationType.title(),
                authorizationType.description()
            ), mouseX, mouseY);
        }

        String text = editorLabelBox.getValue();
        if (!editorLabelBox.isFocused())
            AllGuiTextures.STATION_EDIT_NAME.render(graphics, nameBoxX(text, editorLabelBox) + font.width(text) + 5, y + 1);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y);
        if (!hasPopup)
            graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);
        renderSubAccounts(graphics, mouseX, mouseY, partialTick);

        if (!hasPopup)
            return;

        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);

        int xo = -10;
        int yo = -45;
        NumismaticsGuiTextures.SUB_ACCOUNT_LIST_POPUP.render(graphics, leftPos - 2+xo, topPos + 40+yo);
        renderPlayerInventory(graphics, leftPos + 38+xo, topPos + 167+yo);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (hasPopup) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        float chaseTarget = scroll.getChaseTarget();
        float max = 40 - 173;

        for (SubAccount ignored : menu.contentHolder.getAlphabetizedSubAccounts()) {
            max += CARD_HEADER + CARD_BODY;
        }

        if (max > 0) {
            chaseTarget -= delta * 12;
            chaseTarget = Mth.clamp(chaseTarget, 0, max);
            scroll.chase((int) chaseTarget, 0.7f, LerpedFloat.Chaser.EXP);
        } else {
            scroll.chase(0, 0.7f, LerpedFloat.Chaser.EXP);
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        super.removed();
        syncName();
    }

    private int nameBoxX(String s, EditBox nameBox) {
        return leftPos - 12 + NumismaticsGuiTextures.SUB_ACCOUNT_LIST_POPUP.width / 2 - (Math.min(font.width(s), nameBox.getWidth()) + 10) / 2;
    }
}
