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

package dev.ithundxr.createnumismatics.events;

import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.annotation.event.MultiLoaderEvent;
import dev.ithundxr.createnumismatics.base.block.ConditionalBreak;
import dev.ithundxr.createnumismatics.base.block.NotifyFailedBreak;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.TrustedBlock;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.SubAccount;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlock;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.BankAccountLabelPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Collection;

public class CommonEvents {
    public static void onLoadWorld(LevelAccessor world) {
        Numismatics.BANK.levelLoaded(world);
    }

    /**
     * @return true if the block may be broken, false otherwise
     */
    @MultiLoaderEvent
    public static boolean onBlockBreak(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        if (!(player instanceof ServerPlayer))
            return true;

        boolean mayBreak = true;

        if (state.getBlock() instanceof ConditionalBreak conditionalBreak) {
            mayBreak = conditionalBreak.mayBreak(level, pos, state, player);
        }

        if (state.getBlock() instanceof TrustedBlock trustedBlock && !player.isCrouching() && trustedBlock.isTrusted(player, level, pos)) {
            player.displayClientMessage(Components.translatable("block.numismatics.trusted_block.attempt_break", Components.keybind("key.sneak"))
                    .withStyle(ChatFormatting.DARK_RED), true);
        }


        mayBreak &= !(state.getBlock() instanceof TrustedBlock trustedBlock) || (player.isShiftKeyDown() && trustedBlock.isTrusted(player, level, pos));


        if (!mayBreak && state.getBlock() instanceof NotifyFailedBreak notifyFailedBreak) {
            notifyFailedBreak.notifyFailedBreak(level, pos, state, player);
        }
        return mayBreak;
    }

    @MultiLoaderEvent
    public static void onPlayerJoin(ServerPlayer player) {
        for (BankAccount account : Numismatics.BANK.accounts.values()) {
            NumismaticsPackets.PACKETS.sendTo(player, new BankAccountLabelPacket(account));

            Collection<SubAccount> subAccounts = account.getSubAccounts();
            if (subAccounts != null) {
                for (SubAccount subAccount : subAccounts) {
                    NumismaticsPackets.PACKETS.sendTo(player, new BankAccountLabelPacket(subAccount));
                }
            }
        }
    }

    @MultiLoaderEvent
    public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        boolean offhandFix = !level.isClientSide()
            && !player.getOffhandItem().isEmpty()
            && !(player.getOffhandItem().getItem() instanceof BlockItem) &&
            hand.equals(InteractionHand.MAIN_HAND);
        if ((offhandFix || player.isShiftKeyDown()) && state.getBlock() instanceof VendorBlock vb) {
            return vb.use(state, level, pos, player, hand, hitResult);
        }

        return InteractionResult.PASS;
    }
}
