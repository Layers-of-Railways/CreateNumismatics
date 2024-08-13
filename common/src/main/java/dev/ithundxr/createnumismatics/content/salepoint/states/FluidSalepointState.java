/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.content.salepoint.states;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.compat.computercraft.ComputerCraftProxy;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointBlockEntity;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.IFilteringSalepointBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.SalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.widgets.SalepointFluidConfigWidget;
import dev.ithundxr.createnumismatics.content.salepoint.widgets.SalepointFluidDisplayWidget;
import dev.ithundxr.createnumismatics.multiloader.fluid.FluidUnits;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FluidSalepointState implements ISalepointState<MultiloaderFluidStack> {

    private UUID uuid;
    private @NotNull MultiloaderFluidStack filter = MultiloaderFluidStack.EMPTY;
    private @Nullable Runnable changedCallback;

    @ExpectPlatform
    public static FluidSalepointState create() {
        throw new AssertionError();
    }

    @Override
    public void init() {
        uuid = UUID.randomUUID();
    }

    @Override
    public final SalepointTypes getType() {
        return SalepointTypes.FLUID;
    }

    @Override
    public final UUID getId() {
        return uuid;
    }

    @Override
    public final boolean canChangeFilterTo(MultiloaderFluidStack filter) {
        return (this.filter.isFluidEqual(filter) && !filter.isEmpty()) || canChangeFilterToInternal(filter);
    }

    protected abstract boolean canChangeFilterToInternal(MultiloaderFluidStack filter);

    @Override
    public final boolean setFilter(MultiloaderFluidStack filter, Level salepointLevel, BlockPos salepointPos, @Nullable Player player) {
        if (!canChangeFilterTo(filter))
            return false;

        if (!filter.isEmpty() && salepointLevel.getBlockEntity(salepointPos) instanceof SalepointBlockEntity salepointBE) {
            BlockPos targetedPos = salepointBE.getTargetedPos();
            if (targetedPos != null) {
                var behaviour = getBehaviour(salepointLevel, targetedPos);
                if (behaviour instanceof IFilteringSalepointBehaviour filteringSalepointBehaviour) {
                    if (!filteringSalepointBehaviour.canSetFilter(filter)) {
                        return false;
                    }
                }
            }
        }

        setFilterInternal(filter, salepointLevel, salepointPos, player);
        this.filter = filter.copy();

        setChanged();

        return true;
    }

    protected abstract void setFilterInternal(MultiloaderFluidStack filter, Level salepointLevel, BlockPos salepointPos, @Nullable Player player);

    @Override
    public final MultiloaderFluidStack getFilter() {
        return filter.copy();
    }

    @Override
    public boolean filterMatches(MultiloaderFluidStack object) {
        return filter.isFluidStackIdentical(object);
    }

    @Override
    public final CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", getType().getId());
        tag.putUUID("UUID", uuid);

        saveInternal(tag);

        if (!filter.isEmpty())
            tag.put("Filter", filter.writeToNBT(new CompoundTag()));

        return tag;
    }

    protected abstract void saveInternal(CompoundTag tag);

    @Override
    public final void load(CompoundTag tag) {
        uuid = tag.getUUID("UUID");

        loadInternal(tag);

        if (tag.contains("Filter", CompoundTag.TAG_COMPOUND))
            filter = MultiloaderFluidStack.loadFluidStackFromNBT(tag.getCompound("Filter"));
        else
            filter = MultiloaderFluidStack.EMPTY;
    }

    protected abstract void loadInternal(CompoundTag tag);

    @Override
    public final boolean isValidForPurchase(Level level, BlockPos targetedPos, ReasonHolder reasonHolder) {
        SalepointTargetBehaviour<MultiloaderFluidStack> behaviour = getBehaviour(level, targetedPos);
        return isValidForPurchase(behaviour, reasonHolder);
    }

    protected abstract boolean hasBufferFluidForPurchase();

    protected abstract List<MultiloaderFluidStack> removeBufferFluidForPurchase();

    private boolean isValidForPurchase(@Nullable SalepointTargetBehaviour<MultiloaderFluidStack> behaviour, ReasonHolder reasonHolder) {
        if (behaviour == null) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.no_target"));
            return false;
        }

        if (!behaviour.isUnderControl(this)) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.target_not_controlled"));
            return false;
        }

        if (filter.isEmpty()) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.no_filter"));
            return false;
        }

        if (!hasBufferFluidForPurchase()) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.vendor.out_of_stock"));
            return false;
        }

        if (!behaviour.hasSpaceFor(filter.copy())) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.insufficient_space"));
            return false;
        }

        return true;
    }

    @Override
    public final boolean doPurchase(Level level, BlockPos targetedPos, ReasonHolder reasonHolder) {
        SalepointTargetBehaviour<MultiloaderFluidStack> behaviour = getBehaviour(level, targetedPos);
        if (behaviour == null) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.no_target"));
            return false;
        }

        if (!isValidForPurchase(behaviour, reasonHolder))
            return false;

        if (!behaviour.doPurchase(filter.copy(), this::removeBufferFluidForPurchase)) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.target_failed_purchase"));
            return false;
        }

        return true;
    }

    @Override
    public final void ensureUnderControl(Level level, BlockPos targetedPos) {
        SalepointTargetBehaviour<MultiloaderFluidStack> behaviour = getBehaviour(level, targetedPos);
        if (behaviour == null)
            return;

        behaviour.ensureUnderControl(this);
    }

    @Override
    public final void relinquishControl(Level level, BlockPos targetedPos) {
        SalepointTargetBehaviour<MultiloaderFluidStack> behaviour = getBehaviour(level, targetedPos);
        if (behaviour == null)
            return;

        behaviour.relinquishControl(this);
    }

    @Override
    public void setChangedCallback(Runnable callback) {
        this.changedCallback = callback;
    }

    protected void setChanged() {
        if (changedCallback != null)
            changedCallback.run();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void createConfigWidgets(WidgetConsumer widgetConsumer) {
        widgetConsumer.addRenderableWidget(new SalepointFluidConfigWidget(100, 54, this));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void createPurchaseWidgets(WidgetConsumer widgetConsumer) {
        widgetConsumer.addRenderableWidget(new SalepointFluidDisplayWidget(68, 55, this));
    }

    public static long getFilterCapacity() {
        return FluidUnits.bucket() * 4;
    }

    @Override
    public Map<String, Object> writeForComputerCraft() {
        return Map.of(
            "type", getType().getId(),
            "filter", ComputerCraftProxy.getFluidDetail(filter)
        );
    }

    @Override
    public void createTooltip(List<Component> tooltip, Level level, BlockPos targetedPos) {
        if (filter.isEmpty()) {
            Lang.builder()
                .add(Components.translatable("gui.numismatics.salepoint.fluid_empty"))
                .forGoggles(tooltip);
            return;
        }

        Lang.builder()
            .add(filter.getDisplayName().copy())
            .forGoggles(tooltip);

        Lang.builder()
            .add(Components.literal(TextUtils.formatFluid(filter.getAmount())))
            .style(ChatFormatting.GREEN)
            .forGoggles(tooltip);
    }
}
