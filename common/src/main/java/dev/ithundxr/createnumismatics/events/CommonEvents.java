package dev.ithundxr.createnumismatics.events;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.block.NotifyFailedBreak;
import dev.ithundxr.createnumismatics.content.backend.TrustedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class CommonEvents {
    public static void onLoadWorld(LevelAccessor world) {
        Numismatics.BANK.levelLoaded(world);
    }

    /**
     * @return true if the block may be broken, false otherwise
     */
    public static boolean onBlockBreak(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        boolean result = !(state.getBlock() instanceof TrustedBlock trustedBlock) || (player.isShiftKeyDown() && trustedBlock.isTrusted(player, level, pos));
        if (!result && state.getBlock() instanceof NotifyFailedBreak notifyFailedBreak) {
            notifyFailedBreak.notifyFailedBreak(level, pos, state, player);
        }
        return result;
    }
}
