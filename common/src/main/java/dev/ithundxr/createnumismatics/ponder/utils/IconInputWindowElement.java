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

package dev.ithundxr.createnumismatics.ponder.utils;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.utility.Pointing;
import dev.ithundxr.createnumismatics.mixin.client.AccessorInputWindowElement;
import net.minecraft.world.phys.Vec3;

public class IconInputWindowElement extends InputWindowElement {
    public IconInputWindowElement(Vec3 sceneSpace, Pointing direction) {
        super(sceneSpace, direction);
    }

    public InputWindowElement withIcon(AllIcons icon) {
        ((AccessorInputWindowElement) this).numismatics$setIcon(icon);
        return this;
    }
}
