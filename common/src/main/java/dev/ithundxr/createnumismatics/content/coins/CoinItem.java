package dev.ithundxr.createnumismatics.content.coins;

import com.simibubi.create.foundation.utility.Components;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoinItem extends Item {
    public final Coin coin;
    protected CoinItem(Item.Properties properties, Coin coin) {
        super(properties);
        this.coin = coin;
    }

    public static NonNullFunction<Properties, CoinItem> create(Coin coin) {
        return (properties) -> new CoinItem(properties, coin);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Components.translatable("item.numismatics.coin.tooltip.value", coin.value));
    }
}