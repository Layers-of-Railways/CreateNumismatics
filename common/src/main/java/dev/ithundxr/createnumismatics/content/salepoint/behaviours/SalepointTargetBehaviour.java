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

package dev.ithundxr.createnumismatics.content.salepoint.behaviours;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SalepointTargetBehaviour<C> extends BlockEntityBehaviour {

    public static final BehaviourType<? extends SalepointTargetBehaviour<?>> TYPE = new BehaviourType<>();

    private @Nullable UUID controllerId;

    public SalepointTargetBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public final BehaviourType<?> getType() {
        return TYPE;
    }

    protected abstract Class<C> getContentType();

    public final boolean isControlledBySalepoint() {
        return controllerId != null;
    }

    public final @Nullable UUID getControllingSalepointId() {
        return controllerId;
    }

    /**
     * Try binding to the specified salepoint. If successful, the salepoint type will be returned.
     * @return The type of the salepoint if binding was successful, otherwise null.
     */
    public final @Nullable ISalepointState<?> tryBindToSalepoint() {
        if (isControlledBySalepoint())
            return null;

        ISalepointState<?> salepointState = tryBindToSalepointInternal();
        if (salepointState != null)
            controllerId = salepointState.getId();

        return salepointState;
    }

    protected abstract @Nullable ISalepointState<?> tryBindToSalepointInternal();

    /**
     * Immediately unbind from (or rather cancel the bind to) the specified salepoint by setting controllerId to null. Does not allow implementors to define custom unbind behaviour.
     * <b>Note:</b> Only valid shortly after {@link #tryBindToSalepoint()}, before other methods have been called.
     * @param state The salepoint to unbind from.
     */
    @ApiStatus.Internal
    public final void unbindSalepoint(ISalepointState<?> state) {
        if (controllerId == null || !controllerId.equals(state.getId()))
            return;

        controllerId = null;
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        super.read(nbt, clientPacket);

        if (nbt.hasUUID("ControllingSalepoint"))
            controllerId = nbt.getUUID("ControllingSalepoint");
        else
            controllerId = null;
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        super.write(nbt, clientPacket);
        if (controllerId != null)
            nbt.putUUID("ControllingSalepoint", controllerId);
    }

    protected final boolean isUnderControlSimple(ISalepointState<C> state) {
        return controllerId != null && controllerId.equals(state.getId());
    }

    public final boolean isUnderControl(ISalepointState<C> state) {
        return isUnderControlSimple(state) && isUnderControlInternal(state);
    }

    protected abstract boolean isUnderControlInternal(ISalepointState<C> state);

    public final void ensureUnderControl(ISalepointState<C> state) {
        if (!isControlledBySalepoint())
            controllerId = state.getId();

        if (!isUnderControlSimple(state))
            return;

        ensureUnderControlInternal(state);
    }

    protected abstract void ensureUnderControlInternal(ISalepointState<C> state);

    public final void relinquishControl(ISalepointState<C> state) {
        if (!isUnderControlSimple(state))
            return;

        relinquishControlInternal(state);
        controllerId = null;
    }

    protected abstract void relinquishControlInternal(ISalepointState<C> state);

    public abstract boolean hasSpaceFor(C object);

    public abstract boolean doPurchase(C object, PurchaseProvider<C> purchaseProvider);

    @FunctionalInterface
    public interface PurchaseProvider<C> {
        List<C> extract();
    }

    public static <C> @Nullable SalepointTargetBehaviour<C> get(BlockGetter reader, BlockPos pos, Class<C> type) {
        BlockEntity be;
        try {
            be = reader.getBlockEntity(pos);
        } catch (ConcurrentModificationException e) {
            be = null;
        }
        return get(be, type);
    }

    @SuppressWarnings("unchecked")
    public static <C> @Nullable SalepointTargetBehaviour<C> get(@Nullable BlockEntity be, Class<C> type) {
        SalepointTargetBehaviour<?> behaviour = get(be, TYPE);

        if (behaviour == null)
            return null;
        if (!type.equals(behaviour.getContentType()))
            return null;

        return (SalepointTargetBehaviour<C>) behaviour;
    }
}
