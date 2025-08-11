package dev.ithundxr.createnumismatics.content.checkout;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.data.lang.NumismaticsLangGen;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

/*
    This class mostly contains code extracted from
    com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler.interactWithShop()
    in order to facilitate deferring the order placement. We need to call different parts of that one function at different times.

    As such, it has been isolated to its own class. If the interactWithShop() function changes, most of the impact
    will be to this file, and the mixin which kicks off the whole deferred checkout process
 */
public class CheckoutUtilities {

    public static void shopInteractionSubmitToNetwork(StockTickerBlockEntity tickerBE, PackageOrder order, Player player, Level level, String packageAddress) {
        tickerBE.broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType.PLAYER, order, null, packageAddress);
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ShoppingListItem)
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        if (!order.isEmpty())
            AllSoundEvents.STOCK_TICKER_TRADE.playOnServer(level, tickerBE.getBlockPos());
    }

    public static boolean checkOrderPreconditions(StockTickerBlockEntity tickerBE, PackageOrder order, Level level, Player player) {
        // Must be up-to-date
        tickerBE.getAccurateSummary();

        // Check stock levels
        InventorySummary recentSummary = tickerBE.getRecentSummary();
        for (BigItemStack entry : order.stacks()) {
            if (recentSummary.getCountOf(entry.stack) >= entry.count)
                continue;

            denyPurchase(level, player, "create.stock_keeper.stock_level_too_low");
            return false;
        }
        return true;
    }

    /*
     The "Stock" in the name of this function refers to "Standard" as in this is what we'll call to invoke the standard
     process of placing an order if the order also contains items, instead of just coins.
    */
    public static boolean finishShopInteractionStock(
            StockTickerBlockEntity tickerBE,
            Level level,
            Player player,
            InventorySummary paymentEntries,
            PackageOrder order,
            SmartInventory receivedPayments,
            String packageAddress) {
        if (!checkOrderPreconditions(tickerBE, order, level, player))
            return false;

        // Check space in stock ticker
        int occupiedSlots = 0;
        for (BigItemStack entry : paymentEntries.getStacksByCount())
            occupiedSlots += Mth.ceil(entry.count / (float) entry.stack.getMaxStackSize());
        for (int i = 0; i < receivedPayments.getSlots(); i++)
            if (receivedPayments.getStackInSlot(i)
                    .isEmpty())
                occupiedSlots--;

        if (occupiedSlots > 0) {
            denyPurchase(level, player, "create.stock_keeper.cash_register_full");
            return false;
        }

        // Transfer payment to stock ticker
        for (boolean simulate : Iterate.trueAndFalse) {
            InventorySummary tally = paymentEntries.copy();
            List<ItemStack> toTransfer = new ArrayList<>();

            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack item = player.getInventory()
                        .getItem(i);
                if (item.isEmpty())
                    continue;
                int countOf = tally.getCountOf(item);
                if (countOf == 0)
                    continue;
                int toRemove = Math.min(item.getCount(), countOf);
                tally.add(item, -toRemove);

                if (simulate)
                    continue;

                int newStackSize = item.getCount() - toRemove;
                player.getInventory()
                        .setItem(i, newStackSize == 0 ? ItemStack.EMPTY : item.copyWithCount(newStackSize));
                toTransfer.add(item.copyWithCount(toRemove));
            }

            if (simulate && tally.getTotalCount() != 0) {
                denyPurchase(level, player, "create.stock_keeper.too_broke");
                return false;
            }

            if (simulate)
                continue;

            toTransfer.forEach(s -> ItemHandlerHelper.insertItemStacked(receivedPayments, s, false));
        }

        shopInteractionSubmitToNetwork(tickerBE, order, player, level, packageAddress);
        return true;
    }

    public static void denyPurchase(Level level, Player player, String langKey) {
        AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
        player.displayClientMessage(Component.translatable(langKey).withStyle(ChatFormatting.RED), true);
    }
}
