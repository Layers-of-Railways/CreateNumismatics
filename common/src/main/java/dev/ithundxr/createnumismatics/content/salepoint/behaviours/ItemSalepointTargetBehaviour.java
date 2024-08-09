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
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.content.salepoint.states.SalepointTypes;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemSalepointTargetBehaviour extends SalepointTargetBehaviour<ItemStack> {

    public ItemSalepointTargetBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    protected @Nullable ISalepointState<?> tryBindToSalepointInternal() {
        return SalepointTypes.ITEM.create();
    }

    @Override
    protected @NotNull Class<ItemStack> getContentType() {
        return ItemStack.class;
    }
}
