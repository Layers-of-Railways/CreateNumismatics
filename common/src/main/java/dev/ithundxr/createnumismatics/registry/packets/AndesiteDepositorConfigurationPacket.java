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
