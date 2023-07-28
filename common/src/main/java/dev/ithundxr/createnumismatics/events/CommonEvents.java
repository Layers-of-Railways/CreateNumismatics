package dev.ithundxr.createnumismatics.events;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.block.ConditionalBreak;
import dev.ithundxr.createnumismatics.base.block.NotifyFailedBreak;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.TrustedBlock;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.BankAccountLabelPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
        boolean mayBreak = true;
        if (state.getBlock() instanceof ConditionalBreak conditionalBreak) {
            mayBreak = conditionalBreak.mayBreak(level, pos, state, player);
        }
        mayBreak &= !(state.getBlock() instanceof TrustedBlock trustedBlock) || (player.isShiftKeyDown() && trustedBlock.isTrusted(player, level, pos));
        if (!mayBreak && state.getBlock() instanceof NotifyFailedBreak notifyFailedBreak) {
            notifyFailedBreak.notifyFailedBreak(level, pos, state, player);
        }
        return mayBreak;
    }

    public static void onPlayerJoin(ServerPlayer player) {
        for (BankAccount account : Numismatics.BANK.accounts.values()) {
            NumismaticsPackets.PACKETS.sendTo(player, new BankAccountLabelPacket(account));
        }
    }
}
