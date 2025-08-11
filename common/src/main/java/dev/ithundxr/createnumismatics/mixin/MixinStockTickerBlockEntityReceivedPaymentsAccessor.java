package dev.ithundxr.createnumismatics.mixin;

import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StockTickerBlockEntity.class)
public interface MixinStockTickerBlockEntityReceivedPaymentsAccessor {
    @Accessor
    SmartInventory getReceivedPayments();
}
