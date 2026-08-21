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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.TextWindowElement;
import dev.ithundxr.createnumismatics.mixin_interfaces.PonderOverlayElement_Duck;
import dev.ithundxr.createnumismatics.mixin_interfaces.SceneBuilder_Duck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SceneBuilder.class)
public class MixinSceneBuilder implements SceneBuilder_Duck {
    @Unique
    private boolean numismatics$isOverlayLayer = false;

    @Override
    public void numismatics$setOverlayLayer(boolean enabled) {
        numismatics$isOverlayLayer = enabled;
    }

    @Override
    public boolean numismatics$isOverlayLayerEnabled() {
        return numismatics$isOverlayLayer;
    }

    @Mixin(SceneBuilder.OverlayInstructions.class)
    private static class MixinOverlayInstructions {
        @Shadow @Final
        SceneBuilder this$0;

        @WrapOperation(method = "showText", at = @At(value = "NEW", target = "()Lcom/simibubi/create/foundation/ponder/element/TextWindowElement;"), remap = false)
        private TextWindowElement storeOverlayText(Operation<TextWindowElement> original) {
            TextWindowElement ret = original.call();
            PonderOverlayElement_Duck.numismatics$applyOverlay(this$0, ret);
            return ret;
        }

        @WrapOperation(method = "showSelectionWithText", at = @At(value = "NEW", target = "()Lcom/simibubi/create/foundation/ponder/element/TextWindowElement;"), remap = false)
        private TextWindowElement storeOverlaySelectionText(Operation<TextWindowElement> original) {
            TextWindowElement ret = original.call();
            PonderOverlayElement_Duck.numismatics$applyOverlay(this$0, ret);
            return ret;
        }

        @WrapOperation(method = "showControls", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/ponder/element/InputWindowElement;clone()Lcom/simibubi/create/foundation/ponder/element/InputWindowElement;"), remap = false)
        private InputWindowElement storeOverlayControls(InputWindowElement instance, Operation<InputWindowElement> original) {
            InputWindowElement ret = original.call(instance);
            PonderOverlayElement_Duck.numismatics$applyOverlay(this$0, ret);
            return ret;
        }
    }
}
