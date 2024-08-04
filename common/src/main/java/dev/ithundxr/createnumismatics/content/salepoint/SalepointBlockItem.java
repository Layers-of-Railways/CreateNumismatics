/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.content.salepoint;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.SalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class SalepointBlockItem extends BlockItem {
    public SalepointBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        if (player.isShiftKeyDown() && stack.hasTag()) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            player.displayClientMessage(Components.translatable("block.numismatics.salepoint.tooltip.clear"), true);
            stack.setTag(null);
            AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, .5f);
            return InteractionResult.SUCCESS;
        }

        SalepointTargetBehaviour<?> clickedBehaviour = BlockEntityBehaviour.get(level, pos, SalepointTargetBehaviour.TYPE);
        if (clickedBehaviour != null) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;

            CompoundTag stackTag = stack.getOrCreateTag();
            stackTag.put("SelectedPos", NbtUtils.writeBlockPos(pos));
            player.displayClientMessage(Components.translatable("block.numismatics.salepoint.tooltip.set"), true);
            stack.setTag(stackTag);
            AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, 1);
            return InteractionResult.SUCCESS;
        }

        if (!stack.hasTag()) {
            player.displayClientMessage(Components.translatable("block.numismatics.salepoint.tooltip.missing")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        CompoundTag tag = stack.getTag();
        CompoundTag teTag = new CompoundTag();

        //noinspection DataFlowIssue - tag can't be null here, due to `hasTag` check
        BlockPos selectedPos = NbtUtils.readBlockPos(tag.getCompound("SelectedPos"));
        BlockPos placedPos = pos.relative(context.getClickedFace(), state.canBeReplaced() ? 0 : 1);

        if (!selectedPos.closerThan(placedPos, 16)) {
            player.displayClientMessage(Components.translatable("block.numismatics.salepoint.tooltip.too_far")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        SalepointTargetBehaviour<?> selectedBehaviour = BlockEntityBehaviour.get(level, selectedPos, SalepointTargetBehaviour.TYPE);
        if (selectedBehaviour == null) {
            player.displayClientMessage(Components.translatable("block.numismatics.salepoint.tooltip.not_found")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        ISalepointState<?> salepointState = null;
        if (!level.isClientSide) {
            salepointState = selectedBehaviour.tryBindToSalepoint();

            if (salepointState == null) {
                player.displayClientMessage(Components.translatable("block.numismatics.salepoint.tooltip.not_found")
                    .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            CompoundTag salepointStateTag = new CompoundTag();

            salepointStateTag.put("pos", NbtUtils.writeBlockPos(selectedPos.subtract(placedPos)));
            salepointStateTag.put("state", salepointState.save());

            teTag.put("SalepointState", salepointStateTag);
        }

        tag.put("BlockEntityTag", teTag);

        InteractionResult useOn = super.useOn(context);
        if (level.isClientSide || useOn == InteractionResult.FAIL) {
            if (salepointState != null) {
                selectedBehaviour.unbindSalepoint(salepointState);
            }
            return useOn;
        }

        ItemStack itemInHand = player.getItemInHand(context.getHand());
        if (!itemInHand.isEmpty())
            itemInHand.setTag(null);
        player.displayClientMessage(Components.translatable("block.numismatics.salepoint.tooltip.success")
            .withStyle(ChatFormatting.GREEN), true);

        // TODO perhaps an advancement here?

        return super.useOn(context);
    }
}
