package dev.ithundxr.createnumismatics.content.depositor;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class BrassDepositorBlock extends AbstractDepositorBlock<BrassDepositorBlockEntity> {
    public BrassDepositorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<BrassDepositorBlockEntity> getBlockEntityClass() {
        return BrassDepositorBlockEntity.class;
    }

    @Override
    public BlockEntityType<BrassDepositorBlockEntity> getBlockEntityType() {
        return null; // fixme register
    }
}
