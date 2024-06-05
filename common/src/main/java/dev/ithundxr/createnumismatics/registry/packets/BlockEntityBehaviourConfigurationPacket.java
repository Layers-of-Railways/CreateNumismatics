/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.registry.packets;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import dev.ithundxr.createnumismatics.multiloader.C2SPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

// Copied from Create and adapted to use Numismatics' multiloader packet system
public abstract class BlockEntityBehaviourConfigurationPacket<B extends BlockEntityBehaviour> implements C2SPacket {

    protected BlockPos pos;

    public BlockEntityBehaviourConfigurationPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        readSettings(buf);
    }

    public BlockEntityBehaviourConfigurationPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        writeSettings(buffer);
    }

    protected abstract BehaviourType<B> getType();

    @SuppressWarnings({"ConstantValue", "unchecked"})
    @Override
    public void handle(ServerPlayer sender) {
        Level world = sender.level();
        if (world == null || !world.isLoaded(pos))
            return;
        if (!pos.closerThan(sender.blockPosition(), maxRange()))
            return;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SyncedBlockEntity sbe) {
            if (blockEntity instanceof Trusted trusted && !trusted.isTrusted(sender)) {
                Numismatics.LOGGER.error("Illegal configuration of %s at %s attempted by player %s".formatted(
                    blockEntity, pos, sender
                ));
                sender.connection.disconnect(Components.literal("Haxx: Illegal block entity configuration attempt"));
                return;
            }
            B behaviour = BlockEntityBehaviour.get(sbe, getType());
            if (behaviour != null) {
                applySettings(sender, behaviour);
                if (!causeUpdate())
                    return;
                ((SyncedBlockEntity) blockEntity).sendData();
                blockEntity.setChanged();
            }
        }
    }

    protected int maxRange() {
        return 20;
    }

    protected abstract void writeSettings(FriendlyByteBuf buffer);

    protected abstract void readSettings(FriendlyByteBuf buf);

    protected void applySettings(ServerPlayer player, B behaviour) {
        applySettings(behaviour);
    }

    protected boolean causeUpdate() {
        return true;
    }

    protected abstract void applySettings(B behaviour);
}
