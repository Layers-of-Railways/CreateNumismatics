package dev.ithundxr.createnumismatics.content.checkout;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsGuiTextures;
import dev.ithundxr.createnumismatics.registry.packets.DeferredCheckoutResolutionPacket;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CheckoutScreen extends AbstractSimiContainerScreen<CheckoutMenu> {
    private final NumismaticsGuiTextures background = NumismaticsGuiTextures.CHECKOUT_SCREEN;
    private final ItemStack renderedItem = AllBlocks.STOCK_TICKER.asStack();
    private List<Rect2i> extraAreas = Collections.emptyList();

    public CheckoutScreen(CheckoutMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    private Button payWithCoinsButton;
    private Button payWithCardButton;

    @Override
    protected void init() {
        setWindowSize(background.width, background.height + 2 + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        setWindowOffset(-20, 0);
        super.init();

        int x = leftPos;
        int y = topPos;

        IconButton abortButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_MTD_CLOSE);
        abortButton.withCallback(this::onCancelTransaction);
        addRenderableWidget(abortButton);

        int btnW = 140;
        int btnH = 20;
        int gap = 6;
        int btnX = x + (background.width - btnW - 10) / 2;

        payWithCoinsButton = Button.builder(Component.translatable("gui.numismatics.checkout_screen.pay_with_coins"), b -> onPayWithCoins())
                .pos(btnX, y + 45)
                .size(btnW, btnH)
                .build();
        updatePayWithCoinsButton();
        addRenderableWidget(payWithCoinsButton);

        payWithCardButton = Button.builder(Component.translatable("gui.numismatics.checkout_screen.pay_with_card"), b -> onPayWithCard())
                .pos(btnX, y + 45 + btnH + gap)
                .size(btnW - 24, btnH)
                .build();
        updatePayWithCardButton();
        addRenderableWidget(payWithCardButton);

        extraAreas = ImmutableList.of(new Rect2i(x + background.width, y + background.height - 64, 84, 74));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updatePayWithCoinsButton();
        updatePayWithCardButton();
    }

    private void updatePayWithCoinsButton() {
        if (minecraft == null || minecraft.player == null)
            return;

        var inventory = minecraft.player.getInventory();
        int coinsOnPlayer = coinsInPlayerInventory(inventory);
        payWithCoinsButton.active = menu.contentHolder.costInSpurs() <= coinsOnPlayer;
    }

    private void updatePayWithCardButton() {
        payWithCardButton.active = menu.currentCardUUID != null;
    }

    @Override
    public void onClose() {
        onCancelTransaction();
    }

    private void onPayWithCoins() {
        onConfirmTransaction(CheckoutPaymentMethod.COINS);
    }

    private void onPayWithCard() {
        onConfirmTransaction(CheckoutPaymentMethod.CARD);
    }

    private void onConfirmTransaction(CheckoutPaymentMethod method) {
        CatnipServices.NETWORK.sendToServer(new DeferredCheckoutResolutionPacket(this.menu.contentHolder.id(), method, menu.currentCardUUID));
        super.onClose();
    }

    private void onCancelTransaction() {
        CatnipServices.NETWORK.sendToServer(new DeferredCheckoutResolutionPacket(this.menu.contentHolder.id(), CheckoutPaymentMethod.CANCEL_TRANSACTION, null));
        super.onClose();
    }

    private int coinsInPlayerInventory(Inventory inv) {
        int tally = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.getItem() instanceof CoinItem ci) {
                tally += ci.coin.toSpurs(stack.getCount());
            }
        }
        return tally;
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
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
                        .GuiRenderBuilder>at(x + background.width + 6, y + background.height - 64, -200)
                .scale(5)
                .render(graphics);

        graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

        Couple<Integer> cogsAndSpurs = Coin.COG.convert(menu.contentHolder.costInSpurs());
        int cogs = cogsAndSpurs.getFirst();
        int spurs = cogsAndSpurs.getSecond();
        Component balanceLabel = Component.translatable("gui.numismatics.checkout_screen.total",
                TextUtils.formatInt(cogs), Coin.COG.getName(cogs), spurs);
        graphics.drawCenteredString(font, balanceLabel, x + (background.width - 8) / 2, y + 21, 0xFFFFFF);
    }
}
