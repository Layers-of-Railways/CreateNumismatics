/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.content.coins;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.foundation.utility.Components;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoinItem extends Item {
    public final Coin coin;
    protected CoinItem(Item.Properties properties, Coin coin) {
        super(properties);
        this.coin = coin;
    }

    public static NonNullFunction<Properties, CoinItem> create(Coin coin) {
        return (properties) -> new CoinItem(properties, coin);
    }

    public static boolean extract(Player player, InteractionHand hand, Coin coin, boolean onlyHand) {
        return extract(player, hand, coin, false, onlyHand);
    }

    public static boolean extract(Player player, InteractionHand hand, Map<Coin, Integer> coins, boolean onlyHand) {
        return extract(player, hand, coins, false, onlyHand);
    }

    public static boolean extract(Player player, InteractionHand hand, Coin coin, boolean simulate, boolean onlyHand) {
        return extract(player, hand, ImmutableMap.of(coin, 1), simulate, onlyHand);
    }

    public static boolean extract(Player player, InteractionHand hand, Map<Coin, Integer> coins, boolean simulate, boolean onlyHand) {
        if (!simulate) {
            if (!extract(player, hand, coins, true, onlyHand)) {
                return false;
            }
        }
        DiscreteCoinBag coinBag = DiscreteCoinBag.of(coins);

        List<ItemStack> inventoryList = new ArrayList<>();
        if (onlyHand)
            inventoryList.add(player.getItemInHand(hand));
        else
            inventoryList.addAll(player.getInventory().items);

        return extract(inventoryList, coinBag, simulate);
    }

    public static boolean extract(Container container, Map<Coin, Integer> coins, boolean simulate) {
        if (!simulate) {
            if (!extract(container, coins, true)) {
                return false;
            }
        }
        DiscreteCoinBag coinBag = DiscreteCoinBag.of(coins);

        List<ItemStack> inventoryList = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            inventoryList.add(container.getItem(i));
        }

        return extract(inventoryList, coinBag, simulate);
    }

    public static boolean extract(List<ItemStack> inventoryList, Map<Coin, Integer> coins, boolean simulate) {
        return extract(inventoryList, DiscreteCoinBag.of(coins), simulate);
    }

    private static boolean extract(List<ItemStack> inventoryList, DiscreteCoinBag coinBag, boolean simulate) {
        for (ItemStack stack : inventoryList) {
            if (coinBag.isEmpty())
                return true;
            if (stack.getItem() instanceof CoinItem coinItem) {
                Coin coin = coinItem.coin;
                int needed = coinBag.getDiscrete(coin);
                if (needed == 0)
                    continue;

                int available = stack.getCount();
                int extracted = Math.min(needed, available);
                coinBag.subtract(coin, extracted);
                if (!simulate)
                    stack.setCount(stack.getCount() - extracted);
            }
        }
        return coinBag.isEmpty();
    }

    public static ItemStack setDisplayedCount(ItemStack stack, int amt) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("DisplayedCount", amt);
        stack.setTag(tag);
        return stack;
    }

    public static ItemStack clearDisplayedCount(ItemStack stack) {
        stack.removeTagKey("DisplayedCount");
        return stack;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        Coin descriptor = coin.getDescription();
        if (descriptor == Coin.SPUR) {
            tooltipComponents.add(Components.translatable("item.numismatics.coin.tooltip.value.basic", coin.value));
        } else {
            int relativeValue = coin.value / descriptor.value;
            tooltipComponents.add(Components.translatable("item.numismatics.coin.tooltip.value", relativeValue, descriptor.getName(relativeValue), coin.value));
        }

        int displayedCount;
        if (stack.getTag() != null && (displayedCount = stack.getTag().getInt("DisplayedCount")) > 0) {
            tooltipComponents.add(Components.translatable("item.numismatics.coin.tooltip.count",
                TextUtils.formatInt(displayedCount), coin.getName(displayedCount))
                .withStyle(ChatFormatting.GOLD));
        }
    }
}