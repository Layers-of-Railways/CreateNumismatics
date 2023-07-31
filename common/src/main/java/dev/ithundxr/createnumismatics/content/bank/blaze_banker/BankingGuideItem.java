package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import com.simibubi.create.AllBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BankingGuideItem extends Item {
    public BankingGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();
        Block block = level.getBlockState(clickedPos).getBlock();
        if (block == AllBlocks.BLAZE_BURNER.get() || block == AllBlocks.LIT_BLAZE_BURNER.get()) {
            BlockState state = NumismaticsBlocks.BLAZE_BANKER.getDefaultState();
            if (level.setBlockAndUpdate(clickedPos, state)) {
                state.getBlock().setPlacedBy(level, clickedPos, state, context.getPlayer(), context.getItemInHand());
            }
            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
