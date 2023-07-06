package dev.ithundxr.createnumismatics.content.depositor;

import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class AndesiteDepositorBlock extends AbstractDepositorBlock<AndesiteDepositorBlockEntity> {
    public AndesiteDepositorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<AndesiteDepositorBlockEntity> getBlockEntityClass() {
        return AndesiteDepositorBlockEntity.class;
    }

    @Override
    public BlockEntityType<AndesiteDepositorBlockEntity> getBlockEntityType() {
        return NumismaticsBlockEntities.ANDESITE_DEPOSITOR.get();
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {

        if (hit.getDirection() != Direction.NORTH)
            return InteractionResult.PASS;

        if (state.getValue(POWERED))
            return InteractionResult.FAIL;

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof AndesiteDepositorBlockEntity andesiteDepositor) {
            Coin coin = andesiteDepositor.getCoin();

            if (CoinItem.extract(player, hand, coin)) {
                activate(state, level, pos);
            }

            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.CONSUME;
        }
    }
}
