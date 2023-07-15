package dev.ithundxr.createnumismatics.content.coins;

import com.simibubi.create.foundation.utility.Couple;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface CoinBag {
    void add(Coin coin, int count);

    void subtract(Coin coin, int count);

    void set(Coin coin, int count, int spurRemainder);

    /**
     * @return Couple of (amount of this coin, remainder of spurs)
     */
    Couple<Integer> get(Coin coin);

    ItemStack asStack(Coin coin);

    int getValue();

    default boolean isEmpty() {
        return getValue() == 0;
    }

    CompoundTag save(CompoundTag nbt);

    void load(CompoundTag nbt);

    void clear();
}
