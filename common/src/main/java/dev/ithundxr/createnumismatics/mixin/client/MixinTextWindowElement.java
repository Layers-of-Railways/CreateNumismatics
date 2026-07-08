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

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.element.TextWindowElement;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import dev.ithundxr.createnumismatics.mixin_interfaces.TextWindowElement_Builder_Duck;
import dev.ithundxr.createnumismatics.mixin_interfaces.TextWindowElement_Duck;
import dev.ithundxr.createnumismatics.ponder.utils.ScreenVec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextWindowElement.class)
public class MixinTextWindowElement implements TextWindowElement_Duck {
    @Shadow
    Vec3 vec;

    @Unique
    private @Nullable ScreenVec<?, ?, ?> numismatics$screenVec;

    @Override
    public void numismatics$pointAtPixel(ScreenVec<?, ?, ?> vec) {
        numismatics$screenVec = vec;
        this.vec = null;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/utility/animation/LerpedFloat;settled()Z", ordinal = 0), cancellable = true)
    private void applyScreenVec(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks, float fade,
                                CallbackInfo ci, @Local(name = "sceneToScreen") LocalRef<Vec2> sceneToScreen) {
        if (numismatics$screenVec != null) {
            Vec2 global = numismatics$screenVec.toGlobal(scene, partialTicks);
            if (global == null)
                ci.cancel();
            sceneToScreen.set(global);
        }
    }

    @Definition(id = "vec", field = "Lcom/simibubi/create/foundation/ponder/element/TextWindowElement;vec:Lnet/minecraft/world/phys/Vec3;")
    @Expression("this.vec != null")
    @ModifyExpressionValue(
        method = "render",
        at = @At(value = "MIXINEXTRAS:EXPRESSION"),
        slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/utility/Color;mixColors(IIF)I"))
    )
    private boolean vecIsAlsoScreenVec(boolean orig) {
        return orig || numismatics$screenVec != null;
    }

    @Mixin(TextWindowElement.Builder.class)
    private static class MixinBuilder implements TextWindowElement_Builder_Duck {
        @Shadow @Final
        TextWindowElement this$0;

        @Override
        public void numismatics$pointAtPixel(ScreenVec<?, ?, ?> vec) {
            ((TextWindowElement_Duck) this$0).numismatics$pointAtPixel(vec);
        }
    }
}
