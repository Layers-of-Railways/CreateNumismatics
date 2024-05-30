/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.util;

import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ClientUtils {
    @Environment(EnvType.CLIENT)
    public static boolean testClientPlayer(Predicate<Player> predicate) {
        return predicate.test(Minecraft.getInstance().player);
    }

    private static final ItemStack BARRIER_STACK = new ItemStack(Items.BARRIER);
    public static ItemStack changeGoggleOverlayItem(Supplier<ItemStack> original) {

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return original.get();

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return original.get();

        if (level.getBlockEntity(blockHitResult.getBlockPos()) instanceof VendorBlockEntity vendorBE) {
            // get the block entities cost and show the item for that and its cost and under
            // show what is being sold (the enchants)
            return vendorBE.getSellingItem().isEmpty() ? BARRIER_STACK : vendorBE.getSellingItem();
        }
        return original.get();
    }
}
