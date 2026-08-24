/*
 * Numismatics
 * Copyright (c) 2024-2026 The Railways Team
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
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.SalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.registry.NumismaticsDataComponents;
import net.createmod.catnip.outliner.Outliner;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        if (player.isShiftKeyDown() && stack.has(NumismaticsDataComponents.SALEPOINT_SELECTED_POS)) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            player.displayClientMessage(Component.translatable("block.numismatics.salepoint.tooltip.clear"), true);
            stack.remove(NumismaticsDataComponents.SALEPOINT_SELECTED_POS);
            AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, .5f);
            return InteractionResult.SUCCESS;
        }

        SalepointTargetBehaviour<?> clickedBehaviour = BlockEntityBehaviour.get(level, pos, SalepointTargetBehaviour.TYPE);
        if (clickedBehaviour != null) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;

            stack.set(NumismaticsDataComponents.SALEPOINT_SELECTED_POS, pos);
            player.displayClientMessage(Component.translatable("block.numismatics.salepoint.tooltip.set"), true);
            AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, 1);
            return InteractionResult.SUCCESS;
        }

        BlockPos selectedPos = stack.get(NumismaticsDataComponents.SALEPOINT_SELECTED_POS);
        if (selectedPos == null) {
            player.displayClientMessage(Component.translatable("block.numismatics.salepoint.tooltip.missing")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        CompoundTag teTag = new CompoundTag();

        BlockPos placedPos = pos.relative(context.getClickedFace(), state.canBeReplaced() ? 0 : 1);

        if (!selectedPos.closerThan(placedPos, 16)) {
            player.displayClientMessage(Component.translatable("block.numismatics.salepoint.tooltip.too_far")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        SalepointTargetBehaviour<?> selectedBehaviour = BlockEntityBehaviour.get(level, selectedPos, SalepointTargetBehaviour.TYPE);
        if (selectedBehaviour == null) {
            player.displayClientMessage(Component.translatable("block.numismatics.salepoint.tooltip.not_found")
                .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        ISalepointState<?> salepointState = null;
        if (!level.isClientSide) {
            salepointState = selectedBehaviour.tryBindToSalepoint();

            if (salepointState == null) {
                player.displayClientMessage(Component.translatable("block.numismatics.salepoint.tooltip.not_found")
                    .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }

            CompoundTag salepointStateTag = new CompoundTag();

            salepointStateTag.put("pos", NbtUtils.writeBlockPos(selectedPos.subtract(placedPos)));
            salepointStateTag.put("state", salepointState.save());

            teTag.put("SalepointState", salepointStateTag);
        }

        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(teTag));
        stack.remove(NumismaticsDataComponents.SALEPOINT_SELECTED_POS);

        InteractionResult useOn = super.useOn(context);
        stack.remove(DataComponents.BLOCK_ENTITY_DATA);

        if (level.isClientSide || useOn == InteractionResult.FAIL) {
            if (salepointState != null) {
                selectedBehaviour.unbindSalepoint(salepointState);
            }
            return useOn;
        }

        ItemStack itemInHand = player.getItemInHand(context.getHand());
        if (!itemInHand.isEmpty())
            itemInHand.remove(NumismaticsDataComponents.SALEPOINT_SELECTED_POS);
        player.displayClientMessage(Component.translatable("block.numismatics.salepoint.tooltip.success")
            .withStyle(ChatFormatting.GREEN), true);

        // TODO perhaps an advancement here?

        return super.useOn(context);
    }

    private static @Nullable BlockPos lastShownPos = null;
    private static @Nullable AABB lastShownAABB = null;

    @Environment(EnvType.CLIENT)
    public static void clientTick() {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;
        ItemStack heldItemMainhand = player.getMainHandItem();
        if (!(heldItemMainhand.getItem() instanceof SalepointBlockItem))
            return;

        BlockPos selectedPos = heldItemMainhand.get(NumismaticsDataComponents.SALEPOINT_SELECTED_POS);
        if (selectedPos == null)
            return;

        if (!selectedPos.equals(lastShownPos)) {
            lastShownAABB = getBounds(selectedPos);
            lastShownPos = selectedPos;
        }

        if (lastShownAABB != null)
            Outliner.getInstance().showAABB("target", lastShownAABB)
                .colored(0xffcb74)
                .lineWidth(1 / 16f);
    }

    @Environment(EnvType.CLIENT)
    private static AABB getBounds(BlockPos pos) {
        Level world = Minecraft.getInstance().level;

        BlockState state = world.getBlockState(pos);
        VoxelShape shape = state.getShape(world, pos);
        return shape.isEmpty() ? new AABB(BlockPos.ZERO)
            : shape.bounds()
            .move(pos);
    }
}
