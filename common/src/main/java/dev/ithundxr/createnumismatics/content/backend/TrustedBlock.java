package dev.ithundxr.createnumismatics.content.backend;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;

public interface TrustedBlock {
    boolean isTrusted(Player player, BlockGetter level, BlockPos pos);
}
