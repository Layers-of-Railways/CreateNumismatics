package dev.ithundxr.createnumismatics.content.coins;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.foundation.utility.Components;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
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

    public static boolean extract(Player player, InteractionHand hand, Coin coin) {
        return extract(player, hand, coin, false);
    }

    public static boolean extract(Player player, InteractionHand hand, Map<Coin, Integer> coins) {
        return extract(player, hand, coins, false);
    }

    public static boolean extract(Player player, InteractionHand hand, Coin coin, boolean simulate) {
        return extract(player, hand, ImmutableMap.of(coin, 1), simulate);
    }

    public static boolean extract(Player player, InteractionHand hand, Map<Coin, Integer> coins, boolean simulate) {
        if (!simulate) {
            if (!extract(player, hand, coins, true)) {
                return false;
            }
        }
        CoinBag coinBag = new CoinBag(coins);

        List<ItemStack> inventoryList = new ArrayList<>();
        inventoryList.add(player.getItemInHand(hand));
        inventoryList.addAll(player.getInventory().items);

        for (ItemStack stack : inventoryList) {
            if (coinBag.isEmpty())
                return true;
            if (stack.getItem() instanceof CoinItem coinItem) {
                Coin coin = coinItem.coin;
                int needed = coinBag.get(coin);
                if (needed == 0)
                    continue;

                int available = stack.getCount();
                int extracted = Math.min(needed, available);
                coinBag.subtract(coin, extracted);
                if (!simulate)
                    stack.setCount(stack.getCount() - extracted);
                return true;
            }
        }
        return coinBag.isEmpty();
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
    }
}