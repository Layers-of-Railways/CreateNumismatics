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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ithundxr.createnumismatics.NumismaticsClient;
import dev.ithundxr.createnumismatics.registry.NumismaticsDataComponents;
import dev.ithundxr.createnumismatics.util.UsernameUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
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

    public static ItemStack clear(ItemStack itemStack) {
        itemStack.remove(NumismaticsDataComponents.AUTHORIZATION_PAIR);
        return itemStack;
    }

    public static ItemStack set(ItemStack itemStack, AuthorizationPair pair) {
        itemStack.set(NumismaticsDataComponents.AUTHORIZATION_PAIR, pair);
        return itemStack;
    }

    @Nullable
    public static AuthorizationPair get(ItemStack itemStack) {
        return itemStack.get(NumismaticsDataComponents.AUTHORIZATION_PAIR);
    }

    public static boolean isBound(ItemStack itemStack) {
        return itemStack.has(NumismaticsDataComponents.AUTHORIZATION_PAIR);
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
    public void appendHoverText(
        @NotNull ItemStack stack,
        @NotNull TooltipContext context,
        @NotNull List<Component> tooltipComponents,
        @NotNull TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (isBound(stack)) {
            String name = getPlayerName(stack);
            String label = getAccountLabel(stack);

            if (name == null) {
                tooltipComponents.add(Component.translatable("item.numismatics.card.tooltip.bound")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipComponents.add(Component.translatable("item.numismatics.card.tooltip.bound.to", name)
                    .withStyle(ChatFormatting.GREEN));
            }

            if (label == null) {
                tooltipComponents.add(Component.translatable("item.numismatics.authorized_card.tooltip.bound.no_label")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipComponents.add(Component.translatable("item.numismatics.authorized_card.tooltip.bound.with_label", label)
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
        } else {
            player.displayClientMessage(Component.translatable("item.numismatics.authorized_card.tooltip.hold_shift"), true);
            return InteractionResultHolder.success(handStack);
        }

        return InteractionResultHolder.pass(handStack);
    }

    public record AuthorizationPair(UUID accountID, UUID authorizationID) {
        public static final Codec<AuthorizationPair> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("AccountID").forGetter(AuthorizationPair::accountID),
            UUIDUtil.CODEC.fieldOf("AuthorizationID").forGetter(AuthorizationPair::authorizationID)
        ).apply(i, AuthorizationPair::new));

        public static final StreamCodec<ByteBuf, AuthorizationPair> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AuthorizationPair::accountID,
            UUIDUtil.STREAM_CODEC, AuthorizationPair::authorizationID,
            AuthorizationPair::new
        );

        private static @Nullable AuthorizationPair get(CompoundTag tag) {
            if (!(tag.hasUUID("AccountID") && tag.hasUUID("AuthorizationID")))
                return null;

            UUID accountID = tag.getUUID("AccountID");
            UUID authorizationID = tag.getUUID("AuthorizationID");

            return new AuthorizationPair(accountID, authorizationID);
        }
    }
}
