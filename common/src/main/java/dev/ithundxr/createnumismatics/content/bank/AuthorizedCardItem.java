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

package dev.ithundxr.createnumismatics.content.bank;

import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.NumismaticsClient;
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

public class AuthorizedCardItem extends Item {
    public final DyeColor color;
    public AuthorizedCardItem(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    @SuppressWarnings("DataFlowIssue")
    public static ItemStack clear(ItemStack itemStack) {
        if (!itemStack.hasTag())
            return itemStack;

        CompoundTag tag = itemStack.getTag();
        tag.remove("AccountID");
        tag.remove("AuthorizationID");
        itemStack.setTag(tag);
        return itemStack;
    }

    public static ItemStack set(ItemStack itemStack, AuthorizationPair pair) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putUUID("AccountID", pair.accountID());
        tag.putUUID("AuthorizationID", pair.authorizationID());
        itemStack.setTag(tag);
        return itemStack;
    }

    @SuppressWarnings("DataFlowIssue")
    @Nullable
    public static AuthorizationPair get(ItemStack itemStack) {
        if (!itemStack.hasTag())
            return null;

        CompoundTag tag = itemStack.getTag();
        return AuthorizationPair.get(tag);
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean isBound(ItemStack itemStack) {
        return get(itemStack) != null;
    }

    @Nullable
    public static String getPlayerName(ItemStack itemStack) {
        AuthorizationPair pair = get(itemStack);

        if (pair == null)
            return null;

        return UsernameUtils.INSTANCE.getName(pair.accountID(), null);
    }

    @Nullable
    public static String getAccountLabel(ItemStack itemStack) {
        AuthorizationPair pair = get(itemStack);

        if (pair == null)
            return null;

        return NumismaticsClient.subAccountLabels.getOrDefault(pair.authorizationID(), null);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (isBound(stack)) {
            String name = getPlayerName(stack);
            String label = getAccountLabel(stack);

            if (name == null) {
                tooltipComponents.add(Components.translatable("item.numismatics.card.tooltip.bound")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipComponents.add(Components.translatable("item.numismatics.card.tooltip.bound.to", name)
                    .withStyle(ChatFormatting.GREEN));
            }

            if (label == null) {
                tooltipComponents.add(Components.translatable("item.numismatics.authorized_card.tooltip.bound.no_label")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipComponents.add(Components.translatable("item.numismatics.authorized_card.tooltip.bound.with_label", label)
                    .withStyle(ChatFormatting.GREEN));
            }
        } else {
            tooltipComponents.add(Components.translatable("item.numismatics.card.tooltip.blank"));
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
                player.displayClientMessage(Components.translatable("item.numismatics.id_card.tooltip.cleared"), true);
                return InteractionResultHolder.success(handStack);
            }
        } else {
            player.displayClientMessage(Components.translatable("item.numismatics.authorized_card.tooltip.hold_shift"), true);
            return InteractionResultHolder.success(handStack);
        }

        return InteractionResultHolder.pass(handStack);
    }

    public record AuthorizationPair(UUID accountID, UUID authorizationID) {
        private static @Nullable AuthorizationPair get(CompoundTag tag) {
            if (!(tag.hasUUID("AccountID") && tag.hasUUID("AuthorizationID")))
                return null;

            UUID accountID = tag.getUUID("AccountID");
            UUID authorizationID = tag.getUUID("AuthorizationID");

            return new AuthorizationPair(accountID, authorizationID);
        }
    }
}
