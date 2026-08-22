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


import net.createmod.ponder.api.element.PonderOverlayElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.foundation.element.PonderElementBase;

public interface PonderElementBase_Duck {
    void numismatics$setOverlayLayer(boolean isOverlay);
    boolean numismatics$isOnOverlayLayer();

    static <T extends PonderElementBase & PonderOverlayElement> void numismatics$applyOverlay(SceneBuilder builder, T element) {
        ((PonderElementBase_Duck) element).numismatics$setOverlayLayer(((SceneBuilder_Duck) builder).numismatics$isOverlayLayerEnabled());
    }
}
