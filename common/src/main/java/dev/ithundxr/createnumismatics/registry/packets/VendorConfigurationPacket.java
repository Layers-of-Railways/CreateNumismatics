package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity.Mode;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class VendorConfigurationPacket extends NumismaticsBlockEntityConfigurationPacket<VendorBlockEntity> {
    public static final StreamCodec<ByteBuf, VendorConfigurationPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, i -> i.pos,
        Mode.STREAM_CODEC, i -> i.mode,
        VendorConfigurationPacket::new
    );
    
    private final Mode mode;

    public VendorConfigurationPacket(BlockPos pos, Mode mode) {
        super(pos);
        this.mode = mode;
    }

    @Override
    protected void applySettings(ServerPlayer player, VendorBlockEntity vendorBlockEntity) {
        vendorBlockEntity.setMode(mode);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.VENDOR_CONFIGURATION;
    }
}
