package dev.ithundxr.createnumismatics.content.checkout;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlockEntity;
import dev.ithundxr.createnumismatics.mixin.MixinStockTickerBlockEntityReceivedPaymentsAccessor;
import dev.ithundxr.createnumismatics.multiloader.NumismaticsCheckoutUtilities;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DeferredCheckoutOrder {
    public UUID id;

    private boolean closed = false;

    private final int costInSpurs;
    private final StockTickerBlockEntity stockTicker;
    private final ServerPlayer player;
    private final Level level;
    private final InventorySummary itemCost;
    private final PackageOrder deferredOrder;
    private final String packageAddress;

    public DeferredCheckoutOrder(UUID orderId, ShoppingListItem.ShoppingList list, Level level, ServerPlayer player, StockTickerBlockEntity stockTicker, String packageAddress) {
        Couple<InventorySummary> bakeEntries = list.bakeEntries(level, null);
        InventorySummary paymentEntries = bakeEntries.getSecond();

        // Determine cost of coin component of order
        InventorySummary paymentWithoutCoins = new InventorySummary();
        int cost = 0;
        for (BigItemStack stack : paymentEntries.getStacksByCount()) {
            if (stack.stack.getItem() instanceof CoinItem coinItem) {
                cost += coinItem.coin.toSpurs(stack.count);
            } else {
                paymentWithoutCoins.add(stack);
            }
        }
        costInSpurs = cost;

        this.id = orderId;
        this.itemCost = paymentWithoutCoins;
        this.deferredOrder = new PackageOrder(bakeEntries.getFirst().getStacksByCount());
        this.level = level;
        this.player = player;
        this.stockTicker = stockTicker;
        this.packageAddress = packageAddress;
    }

    public boolean isTransactionValid() {
        if (closed)
            return false;

        if (level.isClientSide)
            return false;

        if (player.hasDisconnected())
            return false;

        if (stockTicker.isRemoved())
            return false;

        if (costInSpurs == 0)
            return false;

        if (getDepositor() == null)
            return false;

        return true;
    }

    public boolean completePurchase(CheckoutPaymentMethod method, @Nullable UUID purchasingAccountId) {
        if (method == CheckoutPaymentMethod.CANCEL_TRANSACTION)
            return false;

        BankAccount account = null;
        if (method == CheckoutPaymentMethod.CARD) {
            if (purchasingAccountId == null) {
                Numismatics.LOGGER.warn("Attempted to complete a card transaction {} with default bank account", id);
                return false;
            }

            account = Numismatics.BANK.getAccount(purchasingAccountId);
            if (account == null) {
                Numismatics.LOGGER.warn("Attempted to complete a card transaction {} with an non-empty, but invalid bank account {}", id, purchasingAccountId);
                return false;
            }

            if (!account.isAuthorized(player)) {
                NumismaticsCheckoutUtilities.denyPurchase(level, player, "numismatics.checkout.unauthorized"); // Unauthorized
                return false;
            }
        }

        if (!isTransactionValid()) {
            Numismatics.LOGGER.warn("Attempted to complete an invalid transaction with UUID " + id);
            return false;
        }

        if (!NumismaticsCheckoutUtilities.checkOrderPreconditions(stockTicker, deferredOrder, level, player)) {
            // checkOrderPreconditions displays the chat message
            return false;
        }

        if (method == CheckoutPaymentMethod.CARD && account.getBalance() < costInSpurs) {
            NumismaticsCheckoutUtilities.denyPurchase(level, player, "numismatics.checkout.insufficient_funds");
            return false;
        }

        if (method == CheckoutPaymentMethod.COINS && !playerHasEnoughCoinsInInventory(player.getInventory(), costInSpurs)) {
            NumismaticsCheckoutUtilities.denyPurchase(level, player, "create.stock_keeper.too_broke");
            return false;
        }

        /*
         So below, we do the transaction two different ways:
         1) if its coins only, we can just skip a lot of steps, do the coin transaction, and submit the package order
            directly to the system. Less points of failure, everyone wins.
         2) if we need to take coins and money, we'll let Create take the items first, then we'll take the coins.
            *in theory* this is safe because:
              a) if its a card transaction, the account balance is sufficient, and the player is authorized
              b) if its a coin transaction, the player has enough coins in their inventory to cover the cost.
            but technically there is a logical flow here that could result in *someone* getting short changed.
            If this does somehow occur, the player will get some free items. If this becomes an issue, we can split the order up
            into two orders, one for coins and one for items, then submit them either back to back, or merge them and submit.
        */
        if (itemCost.isEmpty()) {
            if (!tryDoCoinTransaction(method, account)) {
                Numismatics.LOGGER.warn("Failed to do a numismatics transaction, even though all the preconditions passed!");
                NumismaticsCheckoutUtilities.denyPurchase(level, player, "numismatics.checkout.failure");
                return false;
            }

            // If there's no item cost, we can skip a lot of the default create interaction. and just submit the order.
            NumismaticsCheckoutUtilities.shopInteractionSubmitToNetwork(stockTicker, deferredOrder, player, level, packageAddress);
        } else {
            // There are item costs in the shopping list, so we must submit the order through the standard pipeline.
            // This could potentially fail because of the stock keeper being too full, so we'll submit the order, let it
            // take any items as payment.
            var receivedPayments = ((MixinStockTickerBlockEntityReceivedPaymentsAccessor) stockTicker).getReceivedPayments();
            if (!NumismaticsCheckoutUtilities.finishShopInteractionStock(stockTicker, level, player, itemCost, deferredOrder, receivedPayments, packageAddress)) {
                return false;
            }

            if (!tryDoCoinTransaction(method, account)) {
                Numismatics.LOGGER.warn("Failed to do a numismatics transaction, even though all the preconditions passed! " +
                        "Unfortunately, the stock order has already been placed and we cannot unwind it.");
                NumismaticsCheckoutUtilities.denyPurchase(level, player, "numismatics.checkout.failure");
                return false;
            }
        }
        return true;
    }

    private boolean tryDoCoinTransaction(CheckoutPaymentMethod method, @Nullable BankAccount account) {
        switch (method) {
            case CARD -> {
                if (account == null || !account.isAuthorized(player) || account.getBalance() < costInSpurs) {
                    return false;
                }
                account.deduct(costInSpurs);
            }
            case COINS -> {
                if (!tryPayInSpurs(player.getInventory(), costInSpurs)) {
                    Numismatics.LOGGER.warn("Attempted to pay {} spurs from player {} ({}) inventory, but failed!", costInSpurs, player.getName(), player.getUUID());
                    return false;
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + method);
        }

        depositCoinsToMerchant();
        return true;
    }

    public boolean tryPayInSpurs(Inventory inventory, int spursToRemove) {
        if (spursToRemove <= 0) {
            return true; // nothing to pay
        }

        // 1. Count available coins in the player's inventory
        DiscreteCoinBag available = new DiscreteCoinBag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof CoinItem coinItem) {
                available.add(coinItem.coin, stack.getCount());
            }
        }

        if (available.getValue() < spursToRemove) {
            return false;
        }

        // 2. Plan which coins to remove (greedy from largest to smallest)
        DiscreteCoinBag toRemove = new DiscreteCoinBag();
        int remaining = spursToRemove;
        
        Coin[] coins = Coin.VALUES;
        for (int i = coins.length - 1; i >= 0; i--) {
            Coin coin = coins[i];

            if (remaining <= 0)
                break;

            int canUse = Math.min(available.getDiscrete(coin), remaining / coin.value);
            if (canUse > 0) {
                toRemove.add(coin, canUse);
                remaining -= coin.toSpurs(canUse);
            }
        }

        // 3. If we still have remaining spurs to cover, try to break one larger coin
        // Find the smallest denomination that is strictly larger than 'remaining' and still available
        DiscreteCoinBag changeToAdd = new DiscreteCoinBag();
        if (remaining > 0) {
            // Search ascending for the smallest coin whose value >= remaining and still available
            Coin breaker = null;
            for (Coin coin : Coin.VALUES) {
                int availableCount = available.getDiscrete(coin) - toRemove.getDiscrete(coin);
                if (availableCount > 0 && coin.value >= remaining) {
                    breaker = coin;
                    break;
                }
            }

            if (breaker == null) {
                // Can't cover the remaining with a single larger coin; payment impossible
                return false;
            }

            // Use one breaker coin
            toRemove.add(breaker, 1);
            changeToAdd = DiscreteCoinBag.ofChange(remaining, breaker);
        }

        // 4. Apply Changes
        if (!CoinItem.extract(player, InteractionHand.MAIN_HAND, toRemove.asMap(), true, false)) {
            return false;
        }
        if (!CoinItem.extract(player, InteractionHand.MAIN_HAND, toRemove.asMap(), false, false)) {
            return false;
        }

        // 5. Return change to the player
        for (Coin coin : Coin.values()) {
            int count = changeToAdd.getDiscrete(coin);
            if (count <= 0)
                continue;

            // Split into max stack sizes as needed
            int max = coin.asStack().getMaxStackSize();
            int left = count;
            while (left > 0) {
                int n = Math.min(max, left);
                ItemStack change = coin.asStack(n);
                player.getInventory().placeItemBackInInventory(change);
                left -= n;
            }
        }

        return true;
    }

    private boolean playerHasEnoughCoinsInInventory(Inventory inv, int target) {
        var spursInInventory = 0;
        for (int slot = 0; slot < inv.getContainerSize(); slot++) {
            var stack = inv.getItem(slot);
            if (stack.getItem() instanceof CoinItem coin) {
                spursInInventory += coin.coin.toSpurs(stack.getCount());
                if (spursInInventory >= target)
                    return true;
            }
        }
        return false;
    }

    private AbstractDepositorBlockEntity getDepositor() {
        for (Direction side : Iterate.horizontalDirections) {
            BlockPos pos = stockTicker.getBlockPos().relative(side);
            var e = level.getBlockEntity(pos);
            if (e instanceof AbstractDepositorBlockEntity depositor)
                return depositor;
        }
        return null;
    }

    private void depositCoinsToMerchant() {
        var depositor = getDepositor();
        if (depositor == null)
            return;

        var account = Numismatics.BANK.getAccount(depositor.getDepositAccount());
        if (account != null) {
            account.deposit(costInSpurs);
        } else {
            var coins = DiscreteCoinBag.ofGreedy(costInSpurs);
            for (var c : Coin.values()) {
                depositor.addCoin(c, coins.getDiscrete(c));
            }
        }
    }

    public void close() {
        closed = true;
    }

    public DeferredCheckoutOrderMenuProvider createMenuProvider() {
        return new DeferredCheckoutOrderMenuProvider(id, costInSpurs);
    }
}
