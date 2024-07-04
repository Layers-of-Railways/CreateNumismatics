/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
