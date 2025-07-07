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
