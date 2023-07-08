package dev.ithundxr.createnumismatics.content.bank;

import com.simibubi.create.foundation.utility.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
    public static boolean isBound(ItemStack itemStack) {
        return itemStack.hasTag() && itemStack.getTag().hasUUID("AccountID");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (isBound(stack)) {
            tooltipComponents.add(Components.translatable("item.numismatics.card.tooltip.bound")
                .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Components.translatable("item.numismatics.card.tooltip.blank"));
        }
    }
}
