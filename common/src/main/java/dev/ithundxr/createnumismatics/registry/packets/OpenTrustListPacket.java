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
