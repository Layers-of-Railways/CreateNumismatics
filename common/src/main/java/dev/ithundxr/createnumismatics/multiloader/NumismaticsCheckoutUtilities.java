package dev.ithundxr.createnumismatics.multiloader;

import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class NumismaticsCheckoutUtilities {

    @ExpectPlatform
    public static void shopInteractionSubmitToNetwork(StockTickerBlockEntity tickerBE, PackageOrder order, Player player, Level level, String packageAddress) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean checkOrderPreconditions(StockTickerBlockEntity tickerBE, PackageOrder order, Level level, Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean finishShopInteractionStock(
            StockTickerBlockEntity tickerBE,
            Level level,
            Player player,
            InventorySummary paymentEntries,
            PackageOrder order,
            SmartInventory receivedPayments,
            String packageAddress) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void denyPurchase(Level level, Player player, String langKey) {
        throw new AssertionError();
    }
}
