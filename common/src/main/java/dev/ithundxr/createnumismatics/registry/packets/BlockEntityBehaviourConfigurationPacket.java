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

package dev.ithundxr.createnumismatics.registry.packets;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.AdventureUtil;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

// Copied from Create
public abstract class BlockEntityBehaviourConfigurationPacket<B extends BlockEntityBehaviour> implements ServerboundPacketPayload {
    protected BlockPos pos;
    
    public BlockEntityBehaviourConfigurationPacket(BlockPos pos) {
        this.pos = pos;
    }

    protected abstract BehaviourType<B> getType();
    
    @Override
    public void handle(ServerPlayer player) {
        if (player == null || player.isSpectator() || AdventureUtil.isAdventure(player))
            return;
        Level world = player.level();
        if (!world.isLoaded(this.pos))
            return;
        if (!this.pos.closerThan(player.blockPosition(), maxRange()))
            return;
        BlockEntity blockEntity = world.getBlockEntity(this.pos);
        if (blockEntity instanceof SyncedBlockEntity sbe) {
            if (blockEntity instanceof Trusted trusted && !trusted.isTrusted(player)) {
                Numismatics.LOGGER.error("Illegal configuration of {} at {} attempted by player {}", blockEntity, pos, player);
                player.connection.disconnect(Component.literal("Haxx: Illegal block entity configuration attempt"));
                return;
            }

            B behaviour = BlockEntityBehaviour.get(sbe, getType());
            applySettings(player, behaviour);
            if (!causeUpdate())
                return;
            sbe.sendData();
            blockEntity.setChanged();
        }
    }

    protected int maxRange() {
        return 20;
    }

    protected boolean causeUpdate() {
        return true;
    }

    protected abstract void applySettings(ServerPlayer player, B behaviour);
}
