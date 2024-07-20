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

package dev.ithundxr.createnumismatics.base.item;

import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;

public class SingleCallbackContainer extends DelegatingContainer{

    private final Container container;
    private final ContainerListener onChanged;

    public SingleCallbackContainer(Container container, ContainerListener onChanged) {
        this.container = container;
        this.onChanged = onChanged;
    }

    @Override
    protected Container getContainer() {
        return container;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onChanged.containerChanged(this);
    }
}
