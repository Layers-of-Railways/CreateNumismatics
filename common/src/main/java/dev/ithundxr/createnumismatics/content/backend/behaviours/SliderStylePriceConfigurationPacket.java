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

package dev.ithundxr.createnumismatics.content.backend.behaviours;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.BlockEntityBehaviourConfigurationPacket;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class SliderStylePriceConfigurationPacket extends BlockEntityBehaviourConfigurationPacket<SliderStylePriceBehaviour> {
    public static final StreamCodec<FriendlyByteBuf, SliderStylePriceConfigurationPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, i -> i.pos,
            CatnipStreamCodecBuilders.array(ByteBufCodecs.INT, Integer.class), i -> i.prices,
            SliderStylePriceConfigurationPacket::new
    );
    
    private final Integer[] prices;

    @Override
    protected BehaviourType<SliderStylePriceBehaviour> getType() {
        return SliderStylePriceBehaviour.TYPE;
    }

    public SliderStylePriceConfigurationPacket(BlockPos pos, Integer[] prices) {
        super(pos);
        this.prices = prices;
    }

    public SliderStylePriceConfigurationPacket(SyncedBlockEntity be) {
        super(be.getBlockPos());
        this.prices = new Integer[Coin.values().length];

        SliderStylePriceBehaviour priceBehaviour = BlockEntityBehaviour.get(be, getType());
        priceBehaviour.enableClientRead();
        for (Coin coin : Coin.values()) {
            this.prices[coin.ordinal()] = priceBehaviour.getPrice(coin);
        }
    }

    @Override
    protected void applySettings(ServerPlayer player, SliderStylePriceBehaviour priceBehaviour) {
        for (Coin coin : Coin.values()) {
            priceBehaviour.setPrice(coin, prices[coin.ordinal()]);
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.SLIDER_STYLE_PRICE_CONFIGURATION;
    }
}
