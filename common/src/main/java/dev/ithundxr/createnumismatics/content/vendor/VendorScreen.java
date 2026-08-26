/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
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

package dev.ithundxr.createnumismatics.content.vendor;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import dev.ithundxr.createnumismatics.base.client.rendering.GuiBlockEntityRenderBuilder;
import dev.ithundxr.createnumismatics.base.client.rendering.VirtualizableScreen;
import dev.ithundxr.createnumismatics.config.NumismaticsConfig;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceConfigurationPacket;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity.Mode;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.registry.NumismaticsIcons;
import dev.ithundxr.createnumismatics.registry.packets.ScrollSlotPacket;
import dev.ithundxr.createnumismatics.registry.packets.VendorConfigurationPacket;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static net.createmod.catnip.gui.widget.AbstractSimiWidget.HINT_RGB;

public class VendorScreen extends AbstractSimiContainerScreen<VendorMenu> implements VirtualizableScreen {

    private Indicator extractionIndicator;
    private IconButton extractionButton;
    private IconButton trustListButton;
    private IconButton confirmButton;

    private NumismaticsGuiTextures background;
    private final ItemStack renderedItem;

    private final int COIN_COUNT = Coin.values().length;

    private final Label[] coinLabels = new Label[COIN_COUNT];
    private final ScrollInput[] coinScrollInputs = new ScrollInput[COIN_COUNT];

    private Label modeLabel;
    private SelectionScrollInput modeScrollInput;

    private List<Rect2i> extraAreas = Collections.emptyList();

    private boolean virtualMode = false;
    private @Nullable VirtualHandle virtualHandle = null;

    private Boolean filterActedSpecial = null;

    public VendorScreen(VendorMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        renderedItem = container.contentHolder.isCreativeVendor() ? NumismaticsBlocks.CREATIVE_VENDOR.asStack() : NumismaticsBlocks.VENDOR.asStack();
        background = container.contentHolder.isCreativeVendor() ? NumismaticsGuiTextures.CREATIVE_VENDOR : NumismaticsGuiTextures.VENDOR;
    }

    @Override
    public void markVirtual() {
        virtualMode = true;
        virtualHandle = new VirtualHandle();
    }

    @Override
    public boolean isVirtual() {
        return virtualMode;
    }

    public @NotNull VirtualHandle getVirtualHandle() {
        if (virtualHandle == null)
            throw new IllegalStateException("Not a virtual screen");
        return virtualHandle;
    }

    public @Nullable VirtualHandle getVirtualHandleUnchecked() {
        return virtualHandle;
    }

    public class VirtualHandle {
        private VirtualHandle() {}

        public void setPrice(Coin coin, int amount) {
            ScrollInput input = coinScrollInputs[coin.ordinal()];
            input.setState(amount);
            input.onChanged();
        }

        public void setMode(Mode mode) {
            modeScrollInput.setState(mode.ordinal());
            modeScrollInput.onChanged();
        }

        public void toggleExtraction() {
            extractionButton.onClick(extractionButton.getX() + 4, extractionButton.getY() + 4);
        }
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height + 2 + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        setWindowOffset(-20, 0);
        super.init();

        int x = leftPos;
        int y = topPos;

        boolean extractionButtonActive = menu.contentHolder.getMode() == Mode.BUY;

        extractionIndicator = new Indicator(x + 29, y + background.height - 30, CommonComponents.EMPTY);
        extractionIndicator.state = menu.contentHolder.isAutomatedExtractionEnabled()
            ? (extractionButtonActive ? Indicator.State.GREEN : Indicator.State.ON)
            : (extractionButtonActive ? Indicator.State.RED : Indicator.State.OFF);
        addRenderableWidget(extractionIndicator);

        extractionButton = new IconButton(x + 29, y + background.height - 24, NumismaticsIcons.I_HOPPER);
        extractionButton.withCallback(() -> {
            boolean extractionButtonActive$ = menu.contentHolder.getMode() == Mode.BUY;
            menu.contentHolder.toggleAutomatedExtraction();
            extractionIndicator.state = menu.contentHolder.isAutomatedExtractionEnabled()
                ? (extractionButtonActive$ ? Indicator.State.GREEN : Indicator.State.ON)
                : (extractionButtonActive$ ? Indicator.State.RED : Indicator.State.OFF);
        });
        extractionButton.setToolTip(Component.translatable("gui.numismatics.vendor.toggle_automated_extraction"));
        extractionButton.active = extractionButtonActive;
        addRenderableWidget(extractionButton);

        trustListButton = new IconButton(x + 7, y + background.height - 24, AllIcons.I_VIEW_SCHEDULE);
        trustListButton.setToolTip(Component.translatable("numismatics.trust_list.configure"));
        trustListButton.withCallback(() -> menu.contentHolder.openTrustList());
        addRenderableWidget(trustListButton);

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        for (Coin coin : Coin.values()) {
            int i = coin.ordinal();

            int baseX = x + 36 + 6 + (i < 3 ? 0 : 86 + 54 + 6);

            int yIncrement = 22;
            int baseY = y + 45 + (yIncrement * (i%3));

            coinLabels[i] = new Label(baseX + 18, baseY + 5, CommonComponents.EMPTY).withShadow();
            addRenderableWidget(coinLabels[i]);

            coinScrollInputs[i] = new ScrollInput(baseX, baseY, 36, 18)
                .withRange(0, 129)
                .writingTo(coinLabels[i])
                .titled(Component.literal(TextUtils.titleCaseConversion(coin.getName(0))))
                .calling((value) -> {
                    menu.contentHolder.setPrice(coin, value);
                    menu.contentHolder.disableClientPriceRead();
                    coinLabels[i].setX(baseX + 18 - font.width(coinLabels[i].text) / 2);
                });
            addRenderableWidget(coinScrollInputs[i]);

            coinScrollInputs[i].setState(menu.contentHolder.getPrice(coin));
            coinScrollInputs[i].onChanged();
        }

        modeLabel = new Label(x + 90 + 3 + 9, y + 40 + 5, CommonComponents.EMPTY).withShadow();
        addRenderableWidget(modeLabel);

        modeScrollInput = new SelectionScrollInput(x + 90 + 9, y + 40, 46, 18) {
            @Override
            protected void clampState() {
                super.clampState();
                if (menu.contentHolder.filterActsSpecial())
                    state = Mode.SELL.ordinal();
            }
        };
        modeScrollInput.forOptions(Mode.getComponents());
        modeScrollInput.writingTo(modeLabel);
        modeScrollInput.titled(Component.translatable("block.numismatics.vendor.tooltip.mode"));
        modeScrollInput.calling(idx -> {
            menu.contentHolder.setMode(Mode.values()[idx]);

            boolean extractionButtonActive$ = menu.contentHolder.getMode() == Mode.BUY;
            extractionIndicator.state = menu.contentHolder.isAutomatedExtractionEnabled()
                ? (extractionButtonActive$ ? Indicator.State.GREEN : Indicator.State.ON)
                : (extractionButtonActive$ ? Indicator.State.RED : Indicator.State.OFF);
            extractionButton.active = extractionButtonActive$;

            NumismaticsPackets.PACKETS.send(new VendorConfigurationPacket(menu.contentHolder));
        });
        addRenderableWidget(modeScrollInput);

        modeScrollInput.setState(menu.contentHolder.getMode().ordinal());
        modeScrollInput.onChanged();

        extraAreas = ImmutableList.of(new Rect2i(x + background.width, y + background.height - 68, 84, 84));
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

    @Override
    public void renderTransparentBackground(@NotNull GuiGraphics guiGraphics) {
        if (!isVirtual())
            super.renderTransparentBackground(guiGraphics);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.height + 2;
        renderPlayerInventory(graphics, invX, invY);

        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y);

        GuiGameElement.of(renderedItem).<GuiGameElement
                .GuiRenderBuilder>at(x + background.width + 6, y + background.height - 70, -200)
            .scale(5)
            .render(graphics);

        GuiBlockEntityRenderBuilder.of(menu.contentHolder)
            .<GuiGameElement
                .GuiRenderBuilder>at(x + background.width + 6, y + background.height - 90, -230)
            .scale(5)
            .render(graphics);

        graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

        Couple<Integer> referenceAndSpurs = NumismaticsConfig.common().referenceCoin.get().convert(menu.contentHolder.getTotalPrice());
        int reference = referenceAndSpurs.getFirst();
        int spurs = referenceAndSpurs.getSecond();
        Component balanceLabel = Component.translatable("block.numismatics.brass_depositor.tooltip.price",
            TextUtils.formatInt(reference), NumismaticsConfig.common().referenceCoin.get().getName(reference), spurs);
        graphics.drawCenteredString(font, balanceLabel, x + (background.width - 8) / 2, y + 21, 0xFFFFFF);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        Component component = null;

        if (hoveredSlot != null) {
            if (this.menu.getCarried().isEmpty() && !this.hoveredSlot.hasItem()) {
                if (hoveredSlot.index == VendorMenu.FILTER_SLOT_INDEX) {
                    component = Component.translatable("block.numismatics.vendor.tooltip.trade_item");
                } else if (VendorMenu.INV_START_INDEX <= hoveredSlot.index && hoveredSlot.index < VendorMenu.INV_END_INDEX) {
                    component = Component.translatable("block.numismatics.vendor.tooltip.stock");
                }
            } else if (hoveredSlot.index == VendorMenu.FILTER_SLOT_INDEX) {
                if (!menu.contentHolder.canAcceptFilterStack(menu.getCarried())) {
                    component = Component.translatable("block.numismatics.vendor.tooltip.trade_item.no_filter")
                        .withStyle(Style.EMPTY.withColor(HINT_RGB.getRGB()));
                }
            }
        }

        if (component != null) {
            guiGraphics.renderTooltip(font, component, x, y);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem() && this.hoveredSlot.index == VendorMenu.FILTER_SLOT_INDEX) {
            CatnipServices.NETWORK.sendToServer(new ScrollSlotPacket(this.hoveredSlot.index, scrollY, AllKeys.shiftDown()));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        boolean actsSpecial = menu.contentHolder.filterActsSpecial();
        if (filterActedSpecial == null || filterActedSpecial != actsSpecial) {
            filterActedSpecial = actsSpecial;

            MutableComponent hint = actsSpecial ? Component.translatable("gui.numismatics.vendor.mode_locked") : null;
            modeScrollInput.addHint(hint);
        }
    }

    @Override
    public void removed() {
        CatnipServices.NETWORK.sendToServer(new SliderStylePriceConfigurationPacket(menu.contentHolder));
        CatnipServices.NETWORK.sendToServer(new VendorConfigurationPacket(
            menu.contentHolder.getBlockPos(),
            menu.contentHolder.getMode(),
            menu.contentHolder.isAutomatedExtractionEnabled()
        ));
        super.removed();
    }
}
