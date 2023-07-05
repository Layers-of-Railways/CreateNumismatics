package dev.ithundxr.createnumismatics.content.bank;

import com.simibubi.create.foundation.utility.Couple;

import java.util.Locale;

/*
64 spurs to a cog
8 bevels to a cog
4 sprockets to a cog
8 cogs to a crown
8 crowns to a sun
 */
public enum Coin {
    SPUR(1),
    BEVEL(8),
    SPROCKET(16),
    COG(64),
    CROWN(512),
    SUN(4096)
    ;

    public final int value; // in terms of spurs

    Coin(int value) {
        this.value = value;
    }

    /**
     * Convert this coin to spurs
     * @param amount Number of this coin
     * @return Number of spurs
     */
    public int toSpurs(int amount) {
        return amount * value;
    }

    /**
     * Convert spurs to this coin
     * @param amount Number of spurs
     * @return Couple of (amount of this coin, remainder of spurs)
     */
    public Couple<Integer> convert(int amount) {
        int remainder = amount % value;
        int converted = (amount - remainder) / value;
        return Couple.create(converted, remainder);
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
