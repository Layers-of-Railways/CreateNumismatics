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

package dev.ithundxr.createnumismatics.content.bank;

import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.util.UsernameUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class IDCardItem extends Item {
    public final DyeColor color;
    public IDCardItem(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    @SuppressWarnings("DataFlowIssue")
    public static ItemStack clear(ItemStack itemStack) {
        if (!itemStack.hasTag())
            return itemStack;

        CompoundTag tag = itemStack.getTag();
        tag.remove("UUID");
        itemStack.setTag(tag);
        return itemStack;
    }

    public static ItemStack set(ItemStack itemStack, UUID id) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putUUID("UUID", id);
        itemStack.setTag(tag);
        return itemStack;
    }

    @SuppressWarnings("DataFlowIssue")
    @Nullable
    public static UUID get(ItemStack itemStack) {
        if (!isBound(itemStack))
            return null;

        CompoundTag tag = itemStack.getTag();
        return tag.getUUID("UUID");
    }

    @Nullable
    public static String getPlayerName(ItemStack itemStack) {
        if (!isBound(itemStack))
            return null;
        return UsernameUtils.INSTANCE.getName(get(itemStack));
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean isBound(ItemStack itemStack) {
        return itemStack.hasTag() && itemStack.getTag().hasUUID("UUID");
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack handStack = player.getItemInHand(usedHand);
        if (level.isClientSide)
            return InteractionResultHolder.success(handStack);

        if (isBound(handStack)) {
            if (player.isShiftKeyDown()) {
                clear(handStack);
                player.displayClientMessage(Components.translatable("item.numismatics.id_card.tooltip.cleared"), true);
                return InteractionResultHolder.success(handStack);
            } else {
                player.displayClientMessage(Components.translatable("item.numismatics.id_card.tooltip.already_bound")
                    .withStyle(ChatFormatting.RED), true);
            }
        } else if (!player.isShiftKeyDown()) {
            set(handStack, player.getUUID());
            player.displayClientMessage(Components.translatable("item.numismatics.id_card.tooltip.bound"), true);
            return InteractionResultHolder.success(handStack);
        }

        return InteractionResultHolder.pass(handStack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (isBound(stack)) {
            tooltipComponents.add(Components.translatable("item.numismatics.card.tooltip.bound.to", getPlayerName(stack))
                .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Components.translatable("item.numismatics.card.tooltip.blank"));
        }
    }
}
