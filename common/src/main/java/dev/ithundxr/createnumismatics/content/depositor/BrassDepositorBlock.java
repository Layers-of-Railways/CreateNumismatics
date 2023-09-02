package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceBehaviour;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        return NumismaticsBlockEntities.BRASS_DEPOSITOR.get();
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
                    be -> Utils.openScreen((ServerPlayer) player, be, be::sendToMenu));
            }
            return InteractionResult.SUCCESS;
        }

        if (state.getValue(HORIZONTAL_FACING) != hit.getDirection())
            return InteractionResult.PASS;

        if (state.getValue(POWERED) || state.getValue(LOCKED))
            return InteractionResult.FAIL;

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        SliderStylePriceBehaviour priceBehaviour = BlockEntityBehaviour.get(level, pos, SliderStylePriceBehaviour.TYPE);
        if (priceBehaviour != null && priceBehaviour.deduct(player, hand)) {
            activate(state, level, pos);
        }
        return InteractionResult.CONSUME;
    }
}
