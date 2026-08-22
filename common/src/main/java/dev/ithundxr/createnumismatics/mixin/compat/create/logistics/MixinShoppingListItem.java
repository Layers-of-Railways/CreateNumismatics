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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import net.createmod.catnip.data.Couple;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShoppingListItem.class)
public class MixinShoppingListItem {
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void appendCardHintPre(
        ItemStack pStack,
        Level pLevel,
        List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced,
        CallbackInfo ci,
        @Share("coinCost") LocalBooleanRef coinCost
    ) {
        coinCost.set(false);
    }

    @WrapOperation(method = "appendHoverText", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/tableCloth/ShoppingListItem$ShoppingList;bakeEntries(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Lnet/createmod/catnip/data/Couple;"))
    private Couple<InventorySummary> appendCardHintCheckCost(
        ShoppingListItem.ShoppingList instance,
        LevelAccessor level,
        BlockPos clothPosToIgnore,
        Operation<Couple<InventorySummary>> original,
        @Share("coinCost") LocalBooleanRef coinCost
    ) {
        var res = original.call(instance, level, clothPosToIgnore);
        if (res != null) {
            InventorySummary costs = res.getSecond();
            for (var entry : costs.getStacksByCount()) {
                if (entry.stack.getItem() instanceof CoinItem) {
                    coinCost.set(true);
                }
            }
        }
        return res;
    }

    @Inject(method = "appendHoverText", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/utility/CreateLang;translate(Ljava/lang/String;[Ljava/lang/Object;)Lnet/createmod/catnip/lang/LangBuilder;", ordinal = 3))
    private void appendCardHint(
        ItemStack pStack,
        Level pLevel,
        List<Component> pTooltipComponents,
        TooltipFlag pIsAdvanced,
        CallbackInfo ci,
        @Share("coinCost") LocalBooleanRef coinCost
    ) {
        if (coinCost.get()) {
            CreateLang.translate("table_cloth.hand_to_shop_keeper.numismatics.card")
                .style(ChatFormatting.GRAY)
                .addTo(pTooltipComponents);
        }
    }
}
