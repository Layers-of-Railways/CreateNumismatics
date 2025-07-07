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
        this.prices = new Integer[Coin.values().length];
    }

    public SliderStylePriceConfigurationPacket(SyncedBlockEntity be) {
        super(be.getBlockPos());
        this.prices = new Integer[Coin.values().length];

        SliderStylePriceBehaviour priceBehaviour = BlockEntityBehaviour.get(be, getType());
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
