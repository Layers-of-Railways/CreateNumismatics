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

package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.NumismaticsBlockEntityConfigurationPacket;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class BlazeBankerEditPacket extends NumismaticsBlockEntityConfigurationPacket<BlazeBankerBlockEntity> {
    public static final StreamCodec<ByteBuf, BlazeBankerEditPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, i -> i.pos,
        CatnipStreamCodecBuilders.nullable(ByteBufCodecs.STRING_UTF8), i -> i.label,
        BlazeBankerEditPacket::new
    );
    
//    @Nullable
//    private Boolean allowExtraction;

    @Nullable
    private final String label;

    public BlazeBankerEditPacket(BlockPos pos, @Nullable String label) {
        super(pos);
        this.label = label;
    }

    @Override
    protected void applySettings(ServerPlayer player, BlazeBankerBlockEntity blazeBankerBlockEntity) {
//        if (allowExtraction != null)
//            blazeBankerBlockEntity.setAllowExtraction(allowExtraction);

        if (label != null)
            blazeBankerBlockEntity.setLabel(label); 
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.BLAZE_BANKER_EDIT;
    }
}
