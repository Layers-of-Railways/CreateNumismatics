package dev.ithundxr.createnumismatics.content.coins;

import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.world.item.Item;

public class CoinItem extends Item {
    public final Coin coin;
    protected CoinItem(Item.Properties properties, Coin coin) {
        super(properties);
        this.coin = coin;
    }

    public static NonNullFunction<Properties, CoinItem> create(Coin coin) {
        return (properties) -> new CoinItem(properties, coin);
    }
}