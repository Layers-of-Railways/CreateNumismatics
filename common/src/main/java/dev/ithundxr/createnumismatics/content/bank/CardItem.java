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

public class CardItem extends Item {
    public final DyeColor color;
    public CardItem(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    @SuppressWarnings("DataFlowIssue")
    public static ItemStack clear(ItemStack itemStack) {
        if (!itemStack.hasTag())
            return itemStack;

        CompoundTag tag = itemStack.getTag();
        tag.remove("AccountID");
        itemStack.setTag(tag);
        return itemStack;
    }

    public static ItemStack set(ItemStack itemStack, UUID id) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putUUID("AccountID", id);
        itemStack.setTag(tag);
        return itemStack;
    }

    @SuppressWarnings("DataFlowIssue")
    @Nullable
    public static UUID get(ItemStack itemStack) {
        if (!isBound(itemStack))
            return null;

        CompoundTag tag = itemStack.getTag();
        return tag.getUUID("AccountID");
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean isBound(ItemStack itemStack) {
        return itemStack.hasTag() && itemStack.getTag().hasUUID("AccountID");
    }

    @Nullable
    public static String getPlayerName(ItemStack itemStack) {
        if (!isBound(itemStack))
            return null;
        return UsernameUtils.INSTANCE.getName(get(itemStack), null);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (isBound(stack)) {
            String name = getPlayerName(stack);
            if (name == null) {
                tooltipComponents.add(Components.translatable("item.numismatics.card.tooltip.bound")
                    .withStyle(ChatFormatting.GREEN));
            } else {
                tooltipComponents.add(Components.translatable("item.numismatics.card.tooltip.bound.to", name)
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

        if (isBound(handStack) && player.isShiftKeyDown()) {
            clear(handStack);
            player.displayClientMessage(Components.translatable("item.numismatics.id_card.tooltip.cleared"), true);
            return InteractionResultHolder.success(handStack);
        }

        return InteractionResultHolder.pass(handStack);
    }
}
