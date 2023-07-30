package dev.ithundxr.createnumismatics.content.vendor;

import com.simibubi.create.foundation.block.IBE;
import dev.ithundxr.createnumismatics.content.backend.TrustedBlock;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VendorBlock extends Block implements IBE<VendorBlockEntity>, TrustedBlock {
    public VendorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isTrusted(Player player, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public Class<VendorBlockEntity> getBlockEntityClass() {
        return VendorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends VendorBlockEntity> getBlockEntityType() {
        return NumismaticsBlockEntities.VENDOR.get();
    }
}
