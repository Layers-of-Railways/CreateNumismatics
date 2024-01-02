package dev.ithundxr.createnumismatics.base.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocks implementing this when a break is canceled
 */
public interface NotifyFailedBreak {
    void notifyFailedBreak(LevelAccessor level, BlockPos pos, BlockState state, Player player);
}
