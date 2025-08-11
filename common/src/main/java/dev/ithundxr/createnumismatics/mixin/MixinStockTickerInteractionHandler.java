package dev.ithundxr.createnumismatics.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.checkout.DeferredCheckoutOrderMenuProvider;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StockTickerInteractionHandler.class)
public class MixinStockTickerInteractionHandler {
    @Inject(method = "interactWithShop", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/stockTicker/StockTickerBlockEntity;getAccurateSummary()Lcom/simibubi/create/content/logistics/packager/InventorySummary;"), cancellable = true)
    private static void interactWithShop(
            Player player,
            Level level,
            BlockPos targetPos,
            ItemStack mainHandItem,
            CallbackInfo ci,
            @Local ShoppingListItem.ShoppingList shoppingList,
            @Local StockTickerBlockEntity tickerBE
    ) {

        // Build the deferred order and check to see if all preconditions are being met
        var address = ShoppingListItem.getAddress(mainHandItem);
        var deferredOrder = Numismatics.DEFERRED_ORDERS.deferOrder(shoppingList, level, (ServerPlayer) player, tickerBE, address);
        if (!deferredOrder.isTransactionValid()) {
            // Transaction isn't valid, backout!
            Numismatics.DEFERRED_ORDERS.voidOrder(deferredOrder);
            return;
        }

        // At this point, we've determined this is a numismatics transaction,
        // and we will *not* be allowing the standard trade to complete now. We must defer it
        ci.cancel();

        var deferredOrderModel = deferredOrder.createMenuProvider();
        Utils.openScreen((ServerPlayer) player, deferredOrderModel, deferredOrderModel::sendToMenu);
    }

}
