package dev.ithundxr.createnumismatics.content.backend;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;

public interface TrustedBlock {
    default boolean isTrusted(Player player, BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof Trusted trusted && trusted.isTrusted(player);
    }
}
