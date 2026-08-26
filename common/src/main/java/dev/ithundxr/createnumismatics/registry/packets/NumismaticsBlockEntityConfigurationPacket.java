package dev.ithundxr.createnumismatics.registry.packets;

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.simibubi.create.foundation.utility.AdventureUtil;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class NumismaticsBlockEntityConfigurationPacket<BE extends SyncedBlockEntity> extends BlockEntityConfigurationPacket<BE> {

    protected BlockPos pos;

    public NumismaticsBlockEntityConfigurationPacket(BlockPos pos) {
        super(pos);
        this.pos = pos;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(ServerPlayer player) {
        if (player == null || player.isSpectator() || AdventureUtil.isAdventure(player))
            return;
        Level world = player.level();
        if (!world.isLoaded(this.pos))
            return;
        if (!player.canInteractWithBlock(this.pos, maxRange()))
            return;
        BlockEntity blockEntity = world.getBlockEntity(this.pos);
        if (blockEntity instanceof SyncedBlockEntity) {
            if (blockEntity instanceof Trusted trusted && !trusted.isTrusted(player)) {
                Numismatics.LOGGER.error("Illegal configuration of {} at {} attempted by player {}", blockEntity, pos, player);
                player.connection.disconnect(Component.literal("Haxx: Illegal block entity configuration attempt"));
                return;
            }
            
            applySettings(player, (BE) blockEntity);
            if (!causeUpdate())
                return;
            ((SyncedBlockEntity) blockEntity).sendData();
            blockEntity.setChanged();
        }
    }
}
