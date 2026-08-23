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
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListHolder;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class OpenTrustListPacket<BE extends SyncedBlockEntity & TrustListHolder> extends NumismaticsBlockEntityConfigurationPacket<BE> {
    @SuppressWarnings("rawtypes")
	public static final StreamCodec<ByteBuf, OpenTrustListPacket> STREAM_CODEC = BlockPos.STREAM_CODEC
            .map(OpenTrustListPacket::new, i -> i.pos);

    public OpenTrustListPacket(BlockPos pos) {
        super(pos);
    }

    @Override
    protected void applySettings(ServerPlayer player, BE be) {
        be.openTrustListMenu(player);
    }

    @Override
    protected boolean causeUpdate() {
        return false;
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.OPEN_TRUST_LIST;
    }
}
