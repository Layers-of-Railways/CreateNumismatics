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
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.salepoint.behaviours.SalepointTargetBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableAbstractBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableWrappingEnergyBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.types.Energy;
import dev.ithundxr.createnumismatics.content.salepoint.types.EnergyBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.types.SimpleEnergyBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.widgets.SalepointEnergyConfigWidget;
import dev.ithundxr.createnumismatics.content.salepoint.widgets.SalepointEnergyDisplayWidget;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
public class EnergySalepointState implements ISalepointState<Energy> {

    private UUID uuid;
    private @NotNull Energy filter = new Energy();
    private final EnergyBuffer buffer = new SimpleEnergyBuffer(getFilterCapacity());
    private @NotNull InvalidatableWrappingEnergyBuffer bufferWrapper = createBufferWrapper(buffer);
    private @Nullable Runnable changedCallback;

    @ExpectPlatform
    private static InvalidatableWrappingEnergyBuffer createBufferWrapper(EnergyBuffer buffer) {
        throw new AssertionError();
    }

    EnergySalepointState() {
        buffer.setOnChanged(this::setChanged);
    }

    @Override
    public void init() {
        uuid = UUID.randomUUID();
    }

    @Override
    public SalepointTypes getType() {
        return SalepointTypes.ENERGY;
    }

    @Override
    public UUID getId() {
        return uuid;
    }

    @Override
    public InvalidatableAbstractBuffer<Energy> getBuffer() {
        return bufferWrapper;
    }

    @Override
    public void onDestroy(Level level, BlockPos pos) {
        onUnload();
        buffer.setAmount(0);
    }

    @Override
    public void onUnload() {
        bufferWrapper.invalidate();
    }

    @Override
    public void keepAlive() {
        if (!bufferWrapper.isValid()) {
            bufferWrapper = createBufferWrapper(buffer);
        }
    }

    @Override
    public boolean canChangeFilterTo(Energy filter) {
        return filter.getAmount() <= getFilterCapacity();
    }

    @Override
    public boolean setFilter(Energy filter, Level salepointLevel, BlockPos salepointPos, @Nullable Player player) {
        if (!canChangeFilterTo(filter))
            return false;

        this.filter = filter.copy();
        setChanged();

        return true;
    }

    @Override
    public Energy getFilter() {
        return filter.copy();
    }

    @Override
    public boolean filterMatches(Energy object) {
        return object.getAmount() == filter.getAmount();
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", getType().getId());
        tag.putUUID("UUID", uuid);

        tag.put("Buffer", buffer.write());

        tag.put("Filter", filter.write());

        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        uuid = tag.getUUID("UUID");

        buffer.setAmountNoUpdate(0);
        if (tag.contains("Buffer", Tag.TAG_COMPOUND))
            buffer.read(tag.getCompound("Buffer"));

        if (tag.contains("Filter", Tag.TAG_COMPOUND))
            filter.read(tag.getCompound("Filter"));
        else
            filter.setAmount(0);
    }

    @Override
    public boolean isValidForPurchase(Level level, BlockPos targetedPos, ReasonHolder reasonHolder) {
        SalepointTargetBehaviour<Energy> behaviour = getBehaviour(level, targetedPos);
        return isValidForPurchase(behaviour, reasonHolder);
    }

    private boolean hasBufferEnergyForPurchase() {
        return buffer.getAmount() >= filter.getAmount();
    }

    private List<Energy> removeBufferEnergyForPurchase() {
        buffer.setAmount(buffer.getAmount() - filter.getAmount());
        return List.of(filter.copy());
    }

    private boolean isValidForPurchase(@Nullable SalepointTargetBehaviour<Energy> behaviour, ReasonHolder reasonHolder) {
        if (behaviour == null) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.no_target"));
            return false;
        }

        if (!behaviour.isUnderControl(this)) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.target_not_controlled"));
            return false;
        }

        if (filter.getAmount() == 0) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.no_filter"));
            return false;
        }

        if (!hasBufferEnergyForPurchase()) {
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
    public boolean doPurchase(Level level, BlockPos targetedPos, ReasonHolder reasonHolder) {
        SalepointTargetBehaviour<Energy> behaviour = getBehaviour(level, targetedPos);
        if (behaviour == null) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.no_target"));
            return false;
        }

        if (!isValidForPurchase(behaviour, reasonHolder))
            return false;

        if (!behaviour.doPurchase(filter.copy(), this::removeBufferEnergyForPurchase)) {
            reasonHolder.setMessage(Components.translatable("gui.numismatics.salepoint.target_failed_purchase"));
            return false;
        }

        return true;
    }

    @Override
    public void ensureUnderControl(Level level, BlockPos targetedPos) {
        SalepointTargetBehaviour<Energy> behaviour = getBehaviour(level, targetedPos);
        if (behaviour == null)
            return;

        behaviour.ensureUnderControl(this);
    }

    @Override
    public void relinquishControl(Level level, BlockPos targetedPos) {
        SalepointTargetBehaviour<Energy> behaviour = getBehaviour(level, targetedPos);
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
        widgetConsumer.addRenderableWidget(new SalepointEnergyConfigWidget(100, 54, this));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void createPurchaseWidgets(WidgetConsumer widgetConsumer) {
        widgetConsumer.addRenderableWidget(new SalepointEnergyDisplayWidget(68, 55, this));
    }

    public static long getFilterCapacity() {
        return 1000L * 1000L; // 1 Mfe
    }

    @Override
    public Map<String, Object> writeForComputerCraft() {
        return Map.of(
            "type", getType().getId(),
            "filter", filter.getAmount()
        );
    }

    @Override
    public void createTooltip(List<Component> tooltip, Level level, BlockPos targetedPos) {
        Lang.builder()
            .add(Components.translatable("gui.numismatics.salepoint.energy"))
            .forGoggles(tooltip);

        Lang.builder()
            .add(Components.literal(TextUtils.formatEnergy(filter.getAmount())))
            .style(ChatFormatting.GREEN)
            .forGoggles(tooltip);
    }
}
