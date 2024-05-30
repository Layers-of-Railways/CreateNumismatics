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

package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BankingGuideItem extends Item {
    public BankingGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(clickedPos);
        if (blockEntity != null) {
            if (AllBlockEntityTypes.HEATER.is(blockEntity)) {
                BlockState state = NumismaticsBlocks.BLAZE_BANKER.getDefaultState();
                if (level.setBlockAndUpdate(clickedPos, state)) {
                    state.getBlock().setPlacedBy(level, clickedPos, state, context.getPlayer(), context.getItemInHand());
                }
                context.getItemInHand().shrink(1);
                level.playSound(null, clickedPos, SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 0.5f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }
}
