package dev.ithundxr.createnumismatics.content.depositor;

import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import io.github.fabricators_of_create.porting_lib.util.NetworkHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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

        if (hit.getDirection().getAxis().isVertical()) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            if (isTrusted(player, level, pos)) {
                withBlockEntityDo(level, pos,
                    be -> NetworkHooks.openScreen((ServerPlayer) player, be, be::sendToMenu));
            }
            return InteractionResult.SUCCESS;
        }

        if (state.getValue(HORIZONTAL_FACING) != hit.getDirection())
            return InteractionResult.PASS;

        if (state.getValue(POWERED) || state.getValue(LOCKED))
            return InteractionResult.FAIL;

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof AndesiteDepositorBlockEntity andesiteDepositor) {
            Coin coin = andesiteDepositor.getCoin();

            if (CoinItem.extract(player, hand, coin, true)) {
                activate(state, level, pos);
                andesiteDepositor.inventory.add(coin, 1);
            }

        }
        return InteractionResult.CONSUME;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AndesiteDepositorBlockEntity andesiteDepositorBE) {
            for (Coin coin : Coin.values()) {
                int count = andesiteDepositorBE.inventory.get(coin);
                if (count > 0) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), NumismaticsItems.getCoin(coin).asStack(count));
                    andesiteDepositorBE.inventory.set(coin, 0);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
