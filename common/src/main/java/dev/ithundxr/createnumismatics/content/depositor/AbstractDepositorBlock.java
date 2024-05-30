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

package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import dev.ithundxr.createnumismatics.base.block.NotifyFailedBreak;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.TrustedBlock;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDepositorBlock<T extends AbstractDepositorBlockEntity> extends Block implements
    IWrenchable, IBE<T>, TrustedBlock, NotifyFailedBreak {

    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;

    public AbstractDepositorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(defaultBlockState()
            .setValue(HORIZONTAL_FACING, Direction.NORTH)
            .setValue(POWERED, false)
            .setValue(LOCKED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSignalSource(@NotNull BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        boolean powered = state.getValue(POWERED);

        if (powered) {
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_ALL);
        }
    }

    public void activate(BlockState state, Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel)
            activate(state, serverLevel, pos);
    }

    public void activate(BlockState state, ServerLevel level, BlockPos pos) {
        if (state.getValue(POWERED) || state.getValue(LOCKED)) {
            return;
        }

        level.setBlock(pos, state.setValue(POWERED, true), 3);

        level.playSound(null, pos, SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 0.5f, 1.0f);

        if (!level.getBlockTicks().hasScheduledTick(pos, state.getBlock())) {
            level.scheduleTick(pos, state.getBlock(), 2);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, POWERED, LOCKED);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof AbstractDepositorBlockEntity depositorBE) {
            depositorBE.owner = player.getUUID();
        }
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (!isTrusted(context.getPlayer(), context.getLevel(), context.getClickedPos()))
            return InteractionResult.FAIL;
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (!isTrusted(context.getPlayer(), context.getLevel(), context.getClickedPos()))
            return InteractionResult.FAIL;
        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        if (!isTrusted(player, level, pos)) {
            return 0.0f;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        boolean locked = state.getValue(LOCKED);
        boolean shouldLock = level.hasNeighborSignal(pos);
        if (locked ^ shouldLock) {
            level.setBlock(pos, state.setValue(LOCKED, shouldLock), 2);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AbstractDepositorBlockEntity abstractDepositorBE) {
            for (Coin coin : Coin.values()) {
                int count = abstractDepositorBE.inventory.getDiscrete(coin);
                if (count > 0) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), NumismaticsItems.getCoin(coin).asStack(count));
                    abstractDepositorBE.inventory.setDiscrete(coin, 0);
                }
            }
        }
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public void notifyFailedBreak(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof AbstractDepositorBlockEntity abstractDepositorBE) {
            abstractDepositorBE.notifyDelayedDataSync();
        }
    }
}
