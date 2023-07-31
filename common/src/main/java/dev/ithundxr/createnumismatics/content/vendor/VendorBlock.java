package dev.ithundxr.createnumismatics.content.vendor;

import com.simibubi.create.foundation.block.IBE;
import dev.ithundxr.createnumismatics.content.backend.TrustedBlock;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerBlockEntity;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VendorBlock extends Block implements IBE<VendorBlockEntity>, TrustedBlock {
    public VendorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isTrusted(Player player, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public Class<VendorBlockEntity> getBlockEntityClass() {
        return VendorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends VendorBlockEntity> getBlockEntityType() {
        return NumismaticsBlockEntities.VENDOR.get();
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof VendorBlockEntity vendorBlockEntity) {
            vendorBlockEntity.owner = player.getUUID();
        }
    }
}
