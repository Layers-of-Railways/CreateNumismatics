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
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ithundxr.createnumismatics.mixin_interfaces.PonderUI_Duck;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.createmod.ponder.foundation.ui.PonderUI.ponderPartialTicksPaused;

@Mixin(PonderUI.class)
public class MixinPonderUI implements PonderUI_Duck {
    @Final
    @Shadow private List<PonderScene> scenes;

    @Shadow private int skipCooling;

    @Shadow private boolean identifyMode;

    @Shadow private int index;

    @Unique
    private boolean numismatics$anyVirtualScreens = false;

    @Override
    public boolean numismatics$isIdentifyMode() {
        return identifyMode;
    }

    @Definition(id = "activeScene", local = @Local(type = PonderScene.class, name = "activeScene"))
    @Expression("activeScene = ?")
    @Inject(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void storeVirtualScreens(CallbackInfo ci) {
        boolean[] found = new boolean[]{false};
        scenes.get(index).forEachVisible(VirtualScreenElement.class, $ -> found[0] = true);
        numismatics$anyVirtualScreens = found[0];
    }

    @Inject(method = "updateIdentifiedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;"), cancellable = true)
    private void skipIfVirtualScreens(PonderScene activeScene, CallbackInfo ci) {
        if (numismatics$anyVirtualScreens)
            ci.cancel();
    }

    @Inject(method = "renderOverlay", at = @At(value = "RETURN", ordinal = 0))
    private void renderScreenOverlayAlways(GuiGraphics graphics, int i, float partialTicks, CallbackInfo ci) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        PonderScene story = scenes.get(i);
        PonderUI this$ = (PonderUI) (Object) this;
        story.forEachVisible(VirtualScreenElement.class, e -> e.render(story, this$, graphics,
            skipCooling > 0 ? 0 : identifyMode ? ponderPartialTicksPaused : partialTicks));
        ms.popPose();
    }
}
