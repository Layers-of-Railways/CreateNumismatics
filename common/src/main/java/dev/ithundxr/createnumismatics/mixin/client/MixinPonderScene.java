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
import dev.ithundxr.createnumismatics.mixin_interfaces.PonderElementBase_Duck;
import net.createmod.ponder.api.element.PonderElement;
import net.createmod.ponder.api.element.PonderOverlayElement;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
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

    @WrapWithCondition(method = "lambda$renderOverlay$7", at = @At(value = "INVOKE", target = "Lnet/createmod/ponder/api/element/PonderOverlayElement;render(Lnet/createmod/ponder/foundation/PonderScene;Lnet/createmod/ponder/foundation/ui/PonderUI;Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    private boolean renderNonOverlayFirst(PonderOverlayElement instance, PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks) {
        return !(instance instanceof PonderElementBase_Duck duck) || !duck.numismatics$isOnOverlayLayer();
    }

    @Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void renderOverlayLayer(PonderUI screen, GuiGraphics graphics, float partialTicks, CallbackInfo ci) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(0, 0, 3000);
        forEachVisible(PonderOverlayElement.class, e -> {
            if (e instanceof PonderElementBase_Duck duck && duck.numismatics$isOnOverlayLayer())
                e.render((PonderScene) (Object) this, screen, graphics, partialTicks);
        });
        ms.popPose();
    }
}
