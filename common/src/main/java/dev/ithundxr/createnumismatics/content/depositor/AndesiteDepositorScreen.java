package dev.ithundxr.createnumismatics.content.depositor;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.AndesiteDepositorConfigurationPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class AndesiteDepositorScreen extends AbstractSimiContainerScreen<AndesiteDepositorMenu> {

    private IconButton trustListButton;
    private IconButton confirmButton;

    private SelectionScrollInput coinScrollInput;
    private Label coinLabel;

    private NumismaticsGuiTextures background = NumismaticsGuiTextures.ANDESITE_DEPOSITOR;
    private final ItemStack renderedItem = NumismaticsBlocks.ANDESITE_DEPOSITOR.asStack();

    private List<Rect2i> extraAreas = Collections.emptyList();

    public AndesiteDepositorScreen(AndesiteDepositorMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height + 2 + AllGuiTextures.PLAYER_INVENTORY.height);
        setWindowOffset(-20, 0);
        super.init();

        int x = leftPos;
        int y = topPos;

        trustListButton = new IconButton(x + 19, y + 23, AllIcons.I_VIEW_SCHEDULE);
        trustListButton.withCallback(() -> {
            menu.contentHolder.openTrustList();
        });
        addRenderableWidget(trustListButton);

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            onClose();
        });
        addRenderableWidget(confirmButton);

        coinLabel = new Label(x + 84 + 3, y + 23 + 5, Components.immutableEmpty()).withShadow();
        addRenderableWidget(coinLabel);

        coinScrollInput = new SelectionScrollInput(x + 84, y + 23, 68, 18);
        coinScrollInput.forOptions(Coin.labeledComponents());
        coinScrollInput.writingTo(coinLabel);
        coinScrollInput.titled(Components.translatable("create.numismatics.andesite_depositor.price"));
        coinScrollInput.calling(idx -> {
            // price will be sent when menu closed
            menu.contentHolder.setCoin(Coin.values()[idx]);
            //Numismatics.LOGGER.info("Label: " + coinLabel.text + " Index: " + idx + " Coin: " + Coin.values()[idx].name());
            //coinLabel.setX(x + 84 - font.width(coinLabel.text)/2);
        });
        addRenderableWidget(coinScrollInput);

        coinScrollInput.setState(menu.contentHolder.getCoin().ordinal());
        coinScrollInput.onChanged();

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

        GuiGameElement.of(renderedItem).<GuiGameElement
                .GuiRenderBuilder>at(x + background.width + 6, y + background.height - 70, -200)
            .scale(5)
            .render(graphics);

        graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);
    }

    @Override
    public void removed() {
        super.removed();
        NumismaticsPackets.PACKETS.send(new AndesiteDepositorConfigurationPacket(menu.contentHolder));
    }
}
