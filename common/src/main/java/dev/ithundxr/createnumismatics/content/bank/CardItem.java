/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
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

package dev.ithundxr.createnumismatics.content.bank;

import dev.ithundxr.createnumismatics.registry.NumismaticsDataComponents;
import dev.ithundxr.createnumismatics.util.UsernameUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CardItem extends Item {
    public final DyeColor color;
    public CardItem(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    public static ItemStack clear(ItemStack itemStack) {
        itemStack.remove(NumismaticsDataComponents.CARD_ACCOUNT_ID);
        return itemStack;
    }

    public static ItemStack set(ItemStack itemStack, UUID id) {
        itemStack.set(NumismaticsDataComponents.CARD_ACCOUNT_ID, id);
        return itemStack;
    }

    @Nullable
    public static UUID get(ItemStack itemStack) {
        return itemStack.get(NumismaticsDataComponents.CARD_ACCOUNT_ID);
    }

    public static boolean isBound(ItemStack itemStack) {
        return itemStack.has(NumismaticsDataComponents.CARD_ACCOUNT_ID);
    }

    public static boolean isAnyCardBound(ItemStack itemStack) {
        return CardItem.isBound(itemStack) || AuthorizedCardItem.isBound(itemStack);
    }

    @Nullable
    public static String getPlayerName(ItemStack itemStack) {
        if (!isBound(itemStack))
            return null;
        return UsernameUtils.INSTANCE.getName(get(itemStack), null);
    }

    @Override
    public void appendHoverText(
        @NotNull ItemStack stack,
        @NotNull TooltipContext context,
        @NotNull List<Component> tooltipComponents,
        @NotNull TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (isBound(stack)) {
            String name = getPlayerName(stack);
            if (name == null) {
                tooltipComponents.add(Component.translatable("item.numismatics.card.tooltip.bound")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipComponents.add(Component.translatable("item.numismatics.card.tooltip.bound.to", name)
                    .withStyle(ChatFormatting.GREEN));
            }
        } else {
            tooltipComponents.add(Component.translatable("item.numismatics.card.tooltip.blank"));
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack handStack = player.getItemInHand(usedHand);
        if (level.isClientSide)
            return InteractionResultHolder.success(handStack);

        if (isBound(handStack)) {
            if(player.isShiftKeyDown()) {
                clear(handStack);
                player.displayClientMessage(Component.translatable("item.numismatics.id_card.tooltip.cleared"), true);
                return InteractionResultHolder.success(handStack);
            }
        }
        else {
            set(handStack, player.getUUID());
            level.playSound(null, new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ()), SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 0.5f, 1.0f);
            player.displayClientMessage(Component.translatable("item.numismatics.id_card.tooltip.bound"), true);
            return InteractionResultHolder.success(handStack);
        } return InteractionResultHolder.pass(handStack);
    }
}
