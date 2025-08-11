package dev.ithundxr.createnumismatics.content.checkout;

import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalDeferredCheckoutOrderManager {

    private final Map<UUID, DeferredCheckoutOrder> deferredOrders = new HashMap<>();

    private void warnIfClient() {
        if (Thread.currentThread().getName().equals("Render thread")) {
            long start = System.currentTimeMillis();
            Numismatics.LOGGER.error("Deferred Checkout manager should not be accessed on the client"); // set breakpoint here when developing
            if (Utils.isDevEnv()) {
                long end = System.currentTimeMillis();
                if (end - start < 50) { // crash if breakpoint wasn't set
                    throw new RuntimeException("Illegal checkout performed on client, please set a breakpoint above");
                }
            } else {
                Numismatics.LOGGER.error("Stacktrace: ", new RuntimeException("Illegal checkout access performed on client"));
            }
        }
    }

    public DeferredCheckoutOrder deferOrder(ShoppingListItem.ShoppingList list, Level level, ServerPlayer player, StockTickerBlockEntity stockTicker, String packageAddress) {
        warnIfClient();
        var order = new DeferredCheckoutOrder(UUID.randomUUID(), list, level, player, stockTicker, packageAddress);
        deferredOrders.put(order.id, order);
        return order;
    }

    public DeferredCheckoutOrder getDeferredOrder(UUID id) {
        warnIfClient();
        return deferredOrders.get(id);
    }

    public void voidOrder(DeferredCheckoutOrder order) {
        warnIfClient();
        order.close();
        deferredOrders.remove(order.id);
    }
}
