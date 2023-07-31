package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.base.client.rendering.GuiBlockEntityRenderBuilder;
import dev.ithundxr.createnumismatics.registry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BlazeBankerScreen extends AbstractSimiContainerScreen<BlazeBankerMenu> {

    private EditBox labelBox;

    private IconButton confirmButton;

//    private Indicator toggleExtractionIndicator;
//    private IconButton toggleExtractionButton;

    private final NumismaticsGuiTextures background = NumismaticsGuiTextures.BLAZE_BANKER;
    private final ItemStack renderedItem = NumismaticsBlocks.BLAZE_BANKER.asStack();

    private List<Rect2i> extraAreas = Collections.emptyList();

    public BlazeBankerScreen(BlazeBankerMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height + 2 + AllGuiTextures.PLAYER_INVENTORY.height);
        setWindowOffset(-20, 0);
        super.init();

        int x = leftPos;
        int y = topPos;

        Consumer<String> onTextChanged = s -> labelBox.setX(nameBoxX(s, labelBox));
        labelBox = new EditBox(new NoShadowFontWrapper(font), x + 23, y + 4, background.width - 20, 10,
            Components.literal(menu.contentHolder.getLabelNonNull()));
        labelBox.setBordered(false);
        labelBox.setMaxLength(25);
        labelBox.setTextColor(0x592424);
        labelBox.setValue(menu.contentHolder.getLabelNonNull());
        labelBox.setFocused(false);
        labelBox.mouseClicked(0, 0, 0);
        labelBox.setResponder(onTextChanged);
        labelBox.setX(nameBoxX(labelBox.getValue(), labelBox));
        addRenderableWidget(labelBox);

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        /*toggleExtractionButton = new IconButton(x + 41, y + 89, NumismaticsIcons.I_HOPPER);
        toggleExtractionButton.withCallback(this::toggleExtraction);
        toggleExtractionIndicator = new Indicator(x + 41, y + 81, Components.empty());
        toggleExtractionIndicator.state = menu.contentHolder.allowExtraction() ? Indicator.State.GREEN : Indicator.State.RED;*/
        //addRenderableWidgets(confirmButton, toggleExtractionButton, toggleExtractionIndicator);

        extraAreas = ImmutableList.of(new Rect2i(x + background.width, y + background.height - 64, 84, 74));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (getFocused() != labelBox) {
            labelBox.setCursorPosition(labelBox.getValue()
                .length());
            labelBox.setHighlightPos(labelBox.getCursorPosition());
        }
//        toggleExtractionIndicator.state = menu.contentHolder.allowExtraction() ? Indicator.State.GREEN : Indicator.State.RED;
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

    @Override
    protected void renderForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
        int x = leftPos;
        int y = topPos;

        String text = labelBox.getValue();

        if (!labelBox.isFocused())
            AllGuiTextures.STATION_EDIT_NAME.render(graphics, nameBoxX(text, labelBox) + font.width(text) + 5, y + 1);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.width);
        int invY = topPos + background.height + 2;
        renderPlayerInventory(graphics, invX, invY);

        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y);

        Minecraft mc = Minecraft.getInstance();

        GuiGameElement.of(renderedItem).<GuiGameElement
                .GuiRenderBuilder>at(x + background.width + 6, y + background.height - 70, -200)
            .scale(5)
            .render(graphics);

        GuiBlockEntityRenderBuilder.of(menu.contentHolder)
            .<GuiGameElement
                .GuiRenderBuilder>at(x + background.width + 6, y + background.height - 70, -230)
            .scale(5)
            .render(graphics);

//        graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (!labelBox.isFocused() && pMouseY > topPos && pMouseY < topPos + 14 && pMouseX > leftPos
            && pMouseX < leftPos + background.width) {
            labelBox.setFocused(true);
            labelBox.setHighlightPos(0);
            setFocused(labelBox);
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean hitEnter = getFocused() instanceof EditBox
            && (pKeyCode == InputConstants.KEY_RETURN || pKeyCode == InputConstants.KEY_NUMPADENTER);

        if (hitEnter && labelBox.isFocused()) {
            if (labelBox.getValue().isEmpty())
                labelBox.setValue("Blaze Banker");
            labelBox.setFocused(false);
            syncName();
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

/*    private void toggleExtraction() {
        NumismaticsPackets.PACKETS.send(new BlazeBankerEditPacket(menu.contentHolder.getBlockPos(), !menu.contentHolder.allowExtraction()));
    }*/

    private void syncName() {
        if (!labelBox.getValue().equals(menu.contentHolder.getLabel()))
            setLabel(labelBox.getValue());
    }

    private void setLabel(String label) {
        NumismaticsPackets.PACKETS.send(new BlazeBankerEditPacket(menu.contentHolder.getBlockPos(), label));
    }

    @Override
    public void removed() {
        super.removed();
        if (labelBox == null)
            return;
        syncName();
    }

    private int nameBoxX(String s, EditBox nameBox) {
        return leftPos + background.width / 2 - (Math.min(font.width(s), nameBox.getWidth()) + 10) / 2;
    }
}
