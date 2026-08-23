/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
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
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.IDeductable;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class AndesiteDepositorBlock extends AbstractDepositorBlock<AndesiteDepositorBlockEntity> {
    public AndesiteDepositorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<AndesiteDepositorBlockEntity> getBlockEntityClass() {
        return AndesiteDepositorBlockEntity.class;
    }

    @Override
    public BlockEntityType<AndesiteDepositorBlockEntity> getBlockEntityType() {
        return NumismaticsBlockEntities.ANDESITE_DEPOSITOR.get();
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hitResult.getDirection().getAxis().isVertical()) {
            if (level.isClientSide)
                return ItemInteractionResult.SUCCESS;
            ensureOwned(player, level, pos);
            if (isTrusted(player, level, pos)) {
                withBlockEntityDo(level, pos,
                        be -> Utils.openScreen((ServerPlayer) player, be, be::sendToMenu));
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (state.getValue(HORIZONTAL_FACING) != hitResult.getDirection())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (state.getValue(POWERED) || state.getValue(LOCKED))
            return ItemInteractionResult.FAIL;

        if (level.isClientSide)
            return ItemInteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof AndesiteDepositorBlockEntity andesiteDepositor) {
            Coin coin = andesiteDepositor.getCoin();

            ReasonHolder reasonHolder = new ReasonHolder();
            IDeductable deductable = IDeductable.get(stack, player, reasonHolder);
            if (deductable != null && deductable.deduct(coin, 1, reasonHolder)) {
                activate(state, level, pos);
                andesiteDepositor.addCoin(coin, 1);
            } else if (CoinItem.extract(player, hand, coin, true)) {
                activate(state, level, pos);
                andesiteDepositor.addCoin(coin, 1);
            } else {
                player.displayClientMessage(reasonHolder.getMessageOrDefault()
                        .withStyle(ChatFormatting.DARK_RED), true);
                level.playSound(null, pos, AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);}
        }
        return ItemInteractionResult.CONSUME;
    }
}