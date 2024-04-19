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
public abstract class VendorBlockEntityCapabilities extends SmartBlockEntity implements ICapabilityProvider {
    public VendorBlockEntityCapabilities(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // This is actually just down + all the 5 other sides
    @Unique private static final Direction[] numismatics$DIRECTIONS = {Direction.DOWN, Direction.NORTH};
    @Unique LazyOptional<? extends IItemHandler>[] numismatics$handlers = SidedInvWrapper.create((WorldlyContainer) this, numismatics$DIRECTIONS);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && facing != null && !remove) {
            // If down return the down handler otherwise return the one for all other sides
            return facing == Direction.DOWN ? numismatics$handlers[0].cast() : numismatics$handlers[1].cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        numismatics$handlers = SidedInvWrapper.create((WorldlyContainer) this, numismatics$DIRECTIONS);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        for (LazyOptional<? extends IItemHandler> createNumismatics$handler : numismatics$handlers) {
            createNumismatics$handler.invalidate();
        }
    }
}
