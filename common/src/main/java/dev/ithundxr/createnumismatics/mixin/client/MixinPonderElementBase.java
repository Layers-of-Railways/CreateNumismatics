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

package dev.ithundxr.createnumismatics.mixin.client;

import dev.ithundxr.createnumismatics.mixin_interfaces.PonderElementBase_Duck;
import net.createmod.ponder.foundation.element.PonderElementBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PonderElementBase.class)
public class MixinPonderElementBase implements PonderElementBase_Duck {
    @Unique
    private boolean numismatics$isOverlayLayer = false;

    @Override
    public void numismatics$setOverlayLayer(boolean isOverlay) {
        numismatics$isOverlayLayer = isOverlay;
    }

    @Override
    public boolean numismatics$isOnOverlayLayer() {
        return numismatics$isOverlayLayer;
    }
}
