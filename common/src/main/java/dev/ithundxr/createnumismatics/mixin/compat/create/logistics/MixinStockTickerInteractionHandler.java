/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.mixin.compat.create.logistics;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.IDeductable;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import dev.ithundxr.createnumismatics.content.coins.MergingCoinBag;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(StockTickerInteractionHandler.class)
public class MixinStockTickerInteractionHandler {
    @Definition(id = "i", local = @Local(type = int.class, name = "i"))
    @Expression("i = 0")
    @Inject(
        method = "interactWithShop",
        at = @At(value = "MIXINEXTRAS:EXPRESSION"),
        // constrain things somewhat, for future proofing
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/packager/InventorySummary;copy()Lcom/simibubi/create/content/logistics/packager/InventorySummary;")
        ),
        cancellable = true,
        allow = 1
    )
    private static void applyCard(
        Player player,
        Level level,
        BlockPos targetPos,
        ItemStack mainHandItem,
        CallbackInfo ci,
        @Local(name = "simulate") boolean simulate,
        @Local(name = "tally") InventorySummary tally,
        @Local(name = "toTransfer") List<ItemStack> toTransfer
    ) {

        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty())
            return;

        ReasonHolder reason0 = new ReasonHolder();
        IDeductable deductable = IDeductable.get(offhand, player, reason0);
        if (deductable == null || deductable.getMaxWithdrawal() == 0) {
            if (reason0.hasMessage() || deductable != null) {
                AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
                CreateLang.builder()
                    .add(reason0.getMessageOrDefault())
                    .style(ChatFormatting.RED)
                    .sendStatus(player);
                ci.cancel();
            }
            return;
        }

        MergingCoinBag available = new MergingCoinBag();
        available.add(Coin.SPUR, deductable.getMaxWithdrawal());

        DiscreteCoinBag taken = new DiscreteCoinBag();

        for (Coin coin : Coin.valuesHighToLow()) {
            ItemStack coinStack = available.asStack(coin);
            if (coinStack.isEmpty())
                continue;

            int countOf = tally.getCountOf(coinStack);
            if (countOf == 0)
                continue;

            int toRemove = Math.min(coinStack.getCount(), countOf);
            available.subtract(coin, toRemove);
            tally.add(coinStack, -toRemove);

            if (simulate)
                continue;

            // in the real case, we deduct in one fell swoop
            taken.add(coin, toRemove);
        }

        if (!simulate) {
            // we know that taking items will succeed, but we must ensure the financial
            // transaction succeeds BEFORE items are removed from player inventories

            ReasonHolder reason = new ReasonHolder();
            if (deductable.deduct(taken.getValue(), reason)) {
                for (Coin coin : Coin.values()) {
                    ItemStack stack = taken.asStack(coin);
                    if (!stack.isEmpty())
                        toTransfer.add(stack);
                }
            } else {
                AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
                CreateLang.builder()
                    .add(reason0.getMessageOrDefault())
                    .style(ChatFormatting.RED)
                    .sendStatus(player);
                ci.cancel();
            }
        }
    }
}
