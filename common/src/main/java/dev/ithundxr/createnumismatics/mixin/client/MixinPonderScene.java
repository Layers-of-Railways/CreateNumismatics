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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.element.PonderElement;
import com.simibubi.create.foundation.ponder.element.PonderOverlayElement;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import dev.ithundxr.createnumismatics.mixin_interfaces.PonderOverlayElement_Duck;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(PonderScene.class)
public abstract class MixinPonderScene {
    @Shadow public abstract <T extends PonderElement> void forEachVisible(Class<T> type, Consumer<T> function);

    @WrapWithCondition(method = "lambda$renderOverlay$7", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/ponder/element/PonderOverlayElement;render(Lcom/simibubi/create/foundation/ponder/PonderScene;Lcom/simibubi/create/foundation/ponder/ui/PonderUI;Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    private boolean renderNonOverlayFirst(PonderOverlayElement instance, PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks) {
        return !((PonderOverlayElement_Duck) instance).numismatics$isOnOverlayLayer();
    }

    @Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void renderOverlayLayer(PonderUI screen, GuiGraphics graphics, float partialTicks, CallbackInfo ci) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(0, 0, 3000);
        forEachVisible(PonderOverlayElement.class, e -> {
            if (((PonderOverlayElement_Duck) e).numismatics$isOnOverlayLayer())
                e.render((PonderScene) (Object) this, screen, graphics, partialTicks);
        });
        ms.popPose();
    }
}
