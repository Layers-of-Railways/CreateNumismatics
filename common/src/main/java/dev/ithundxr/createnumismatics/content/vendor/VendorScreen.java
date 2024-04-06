package dev.ithundxr.createnumismatics.content.vendor;

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
import dev.ithundxr.createnumismatics.base.client.rendering.GuiBlockEntityRenderBuilder;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceConfigurationPacket;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class VendorScreen extends AbstractSimiContainerScreen<VendorMenu> {

    private IconButton trustListButton;
    private IconButton confirmButton;

    private NumismaticsGuiTextures background;
    private final ItemStack renderedItem;

    private final int COIN_COUNT = Coin.values().length;

    private final Label[] coinLabels = new Label[COIN_COUNT];
    private ScrollInput[] coinScrollInputs = new ScrollInput[COIN_COUNT];

    private List<Rect2i> extraAreas = Collections.emptyList();

    public VendorScreen(VendorMenu container, Inventory inv, Component title) {
        super(container, inv, title);
        renderedItem = container.contentHolder.isCreativeVendor() ? NumismaticsBlocks.CREATIVE_VENDOR.asStack() : NumismaticsBlocks.VENDOR.asStack();
        background = container.contentHolder.isCreativeVendor() ? NumismaticsGuiTextures.CREATIVE_VENDOR : NumismaticsGuiTextures.VENDOR;
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height + 2 + AllGuiTextures.PLAYER_INVENTORY.height);
        setWindowOffset(-20, 0);
        super.init();

        int x = leftPos;
        int y = topPos;

        trustListButton = new IconButton(x + 7, y + 121, AllIcons.I_VIEW_SCHEDULE);
        trustListButton.withCallback(() -> {
            menu.contentHolder.openTrustList();
        });
        addRenderableWidget(trustListButton);

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            onClose();
        });
        addRenderableWidget(confirmButton);

        for (Coin coin : Coin.values()) {
            int i = coin.ordinal();

            int baseX = x + 36 + (i < 3 ? 0 : 86 + 54);

            int yIncrement = 22;
            int baseY = y + 45 + (yIncrement * (i%3));

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

        Couple<Integer> cogsAndSpurs = Coin.COG.convert(menu.contentHolder.getTotalPrice());
        int cogs = cogsAndSpurs.getFirst();
        int spurs = cogsAndSpurs.getSecond();
        Component balanceLabel = Components.translatable("block.numismatics.brass_depositor.tooltip.price",
            TextUtils.formatInt(cogs), Coin.COG.getName(cogs), spurs);
        graphics.drawCenteredString(font, balanceLabel, x + (background.width - 8) / 2, y + 21, 0xFFFFFF);
    }

    @Override
    public void removed() {
        NumismaticsPackets.PACKETS.send(new SliderStylePriceConfigurationPacket(menu.contentHolder));
        super.removed();
    }
}
