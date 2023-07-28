package dev.ithundxr.createnumismatics.base.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface ConditionalBreak {
    boolean mayBreak(LevelAccessor level, BlockPos pos, BlockState state, Player player);
}
