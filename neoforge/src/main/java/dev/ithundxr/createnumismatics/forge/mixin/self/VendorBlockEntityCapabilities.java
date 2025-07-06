package dev.ithundxr.createnumismatics.forge.mixin.self;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VendorBlockEntity.class)
public abstract class VendorBlockEntityCapabilities extends SmartBlockEntity implements ICapabilityProvider, WorldlyContainer {
    public VendorBlockEntityCapabilities(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique LazyOptional<? extends IItemHandler> numismatics$handler = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.NORTH));
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !remove) {
            // If down return the down handler otherwise return the one for all other sides
            return numismatics$handler.cast();
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        numismatics$handler = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.NORTH));    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        numismatics$handler.invalidate();
    }
}
