package dev.ithundxr.createnumismatics.registry.packets;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import dev.ithundxr.createnumismatics.multiloader.C2SPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

// Copied from Create and adapted to use Numismatics' multiloader packet system
public abstract class BlockEntityConfigurationPacket<BE extends SyncedBlockEntity> implements C2SPacket {

    protected BlockPos pos;

    public BlockEntityConfigurationPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        readSettings(buf);
    }

    public BlockEntityConfigurationPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        writeSettings(buffer);
    }

    @SuppressWarnings({"ConstantValue", "unchecked"})
    @Override
    public void handle(ServerPlayer sender) {
        Level world = sender.level;
        if (world == null || !world.isLoaded(pos))
            return;
        if (!pos.closerThan(sender.blockPosition(), maxRange()))
            return;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SyncedBlockEntity) {
            if (blockEntity instanceof Trusted trusted && !trusted.isTrusted(sender)) {
                Numismatics.LOGGER.error("Illegal configuration of %s at %s attempted by player %s".formatted(
                    blockEntity, pos, sender
                ));
                sender.connection.disconnect(Components.literal("Haxx: Illegal block entity configuration attempt"));
                return;
            }
            applySettings(sender, (BE) blockEntity);
            if (!causeUpdate())
                return;
            ((SyncedBlockEntity) blockEntity).sendData();
            blockEntity.setChanged();
        }
    }

    protected int maxRange() {
        return 20;
    }

    protected abstract void writeSettings(FriendlyByteBuf buffer);

    protected abstract void readSettings(FriendlyByteBuf buf);

    protected void applySettings(ServerPlayer player, BE be) {
        applySettings(be);
    }

    protected boolean causeUpdate() {
        return true;
    }

    protected abstract void applySettings(BE be);
}
