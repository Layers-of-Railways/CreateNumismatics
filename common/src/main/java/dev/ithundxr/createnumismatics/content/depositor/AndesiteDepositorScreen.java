package dev.ithundxr.createnumismatics.content.depositor;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class AndesiteDepositorScreen extends AbstractSimiContainerScreen<AndesiteDepositorMenu> {

    private IconButton confirmButton;

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
    protected void renderBg(@NotNull PoseStack ms, float partialTick, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.width);
        int invY = topPos + background.height + 2;
        renderPlayerInventory(ms, invX, invY);

        int x = leftPos;
        int y = topPos;

        background.render(ms, x, y, this);

        GuiGameElement.of(renderedItem).<GuiGameElement
                .GuiRenderBuilder>at(x + background.width + 6, y + background.height - 70, -200)
            .scale(5)
            .render(ms);

        drawCenteredString(ms, font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);
    }
}
