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

package dev.ithundxr.createnumismatics.content.backend;

import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

public interface Trusted {
    @OverrideOnly
    boolean isTrustedInternal(Player player);

    @NonExtendable
    default boolean isTrusted(Player player) {
        return isForceTrusted(player) || isTrustedInternal(player);
    }

    static boolean isForceTrusted(Player player) {
        if (Utils.isDevEnv())
            return player.getItemBySlot(EquipmentSlot.LEGS).is(Items.GOLDEN_LEGGINGS);
        return player.hasPermissions(2);
    }
}
