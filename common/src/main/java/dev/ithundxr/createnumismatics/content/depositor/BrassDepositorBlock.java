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

package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceBehaviour;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class BrassDepositorBlock extends AbstractDepositorBlock<BrassDepositorBlockEntity> {
    public BrassDepositorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<BrassDepositorBlockEntity> getBlockEntityClass() {
        return BrassDepositorBlockEntity.class;
    }

    @Override
    public BlockEntityType<BrassDepositorBlockEntity> getBlockEntityType() {
        return NumismaticsBlockEntities.BRASS_DEPOSITOR.get();
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {

        if (hit.getDirection().getAxis().isVertical()) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            if (isTrusted(player, level, pos)) {
                withBlockEntityDo(level, pos,
                    be -> Utils.openScreen((ServerPlayer) player, be, be::sendToMenu));
            }
            return InteractionResult.SUCCESS;
        }

        if (state.getValue(HORIZONTAL_FACING) != hit.getDirection())
            return InteractionResult.PASS;

        if (state.getValue(POWERED) || state.getValue(LOCKED))
            return InteractionResult.FAIL;

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        ReasonHolder reasonHolder = new ReasonHolder();
        SliderStylePriceBehaviour priceBehaviour = BlockEntityBehaviour.get(level, pos, SliderStylePriceBehaviour.TYPE);
        if (priceBehaviour != null && priceBehaviour.deduct(player, hand, true, reasonHolder)) {
            activate(state, level, pos);
        } else {
            player.displayClientMessage(reasonHolder.getMessageOrDefault()
                    .withStyle(ChatFormatting.DARK_RED), true);
            level.playSound(null, pos, AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);}
        return InteractionResult.CONSUME;
    }
}