package dev.ithundxr.createnumismatics.registry.packets;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class OpenTrustListPacket<BE extends SyncedBlockEntity & TrustListHolder> extends BlockEntityConfigurationPacket<BE> {
    public OpenTrustListPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public OpenTrustListPacket(BE be) {
        super(be.getBlockPos());
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {}

    @Override
    protected void readSettings(FriendlyByteBuf buf) {}

    @Override
    protected void applySettings(BE be) {}

    @Override
    protected void applySettings(ServerPlayer player, BE be) {
        be.openTrustListMenu(player);
    }

    @Override
    protected boolean causeUpdate() {
        return false;
    }
}
