/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.mixin_interfaces;

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.element.PonderOverlayElement;

public interface PonderOverlayElement_Duck {
    void numismatics$setOverlayLayer(boolean isOverlay);
    boolean numismatics$isOnOverlayLayer();

    static void numismatics$applyOverlay(SceneBuilder builder, PonderOverlayElement element) {
        ((PonderOverlayElement_Duck) element).numismatics$setOverlayLayer(((SceneBuilder_Duck) builder).numismatics$isOverlayLayerEnabled());
    }
}
