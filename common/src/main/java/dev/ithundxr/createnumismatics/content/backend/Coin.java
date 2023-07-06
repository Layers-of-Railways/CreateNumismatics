package dev.ithundxr.createnumismatics.content.backend;

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
    BEVEL(8), // 8 spurs
    SPROCKET(16), // 16 spurs, 2 bevels
    COG(64), // 64 spurs, 8 bevels, 4 sprockets
    CROWN(512), // 512 spurs, 64 bevels, 32 sprockets, 8 cogs
    SUN(4096) // 4096 spurs, 512 bevels, 256 sprockets, 64 cogs, 8 crowns
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
        if (this == SPUR) return Couple.create(amount, 0);
        int remainder = amount % value;
        int converted = (amount - remainder) / value;
        return Couple.create(converted, remainder);
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getName(int amount) {
        return getName() + (amount != 1 ? "s" : "");
    }
}
