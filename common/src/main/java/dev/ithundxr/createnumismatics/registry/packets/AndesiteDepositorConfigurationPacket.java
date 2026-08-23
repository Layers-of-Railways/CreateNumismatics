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

import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlockEntity;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class AndesiteDepositorConfigurationPacket extends NumismaticsBlockEntityConfigurationPacket<AndesiteDepositorBlockEntity> {
    public static final StreamCodec<ByteBuf, AndesiteDepositorConfigurationPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, i -> i.pos, 
        Coin.STREAM_CODEC, i -> i.coin,
        AndesiteDepositorConfigurationPacket::new
    );
    
    private final Coin coin;

    public AndesiteDepositorConfigurationPacket(BlockPos pos, Coin coin) {
        super(pos);
        this.coin = coin;
    }

    @Override
    protected void applySettings(ServerPlayer player, AndesiteDepositorBlockEntity andesiteDepositorBlockEntity) {
        andesiteDepositorBlockEntity.setCoin(coin);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.ANDESITE_DEPOSITOR_CONFIGURATION;
    }
}
