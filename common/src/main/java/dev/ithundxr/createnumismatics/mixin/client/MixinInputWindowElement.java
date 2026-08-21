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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.ui.PonderUI;
import dev.ithundxr.createnumismatics.mixin_interfaces.InputWindowElement_Duck;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InputWindowElement.class)
public class MixinInputWindowElement implements InputWindowElement_Duck {
    @Shadow
    ItemStack item;

    @Unique
    private boolean numismatics$showItemCount = false;

    @Override
    public void numismatics$showItemCount(boolean show) {
        numismatics$showItemCount = show;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/gui/element/GuiGameElement;of(Lnet/minecraft/world/item/ItemStack;)Lcom/simibubi/create/foundation/gui/element/GuiGameElement$GuiRenderBuilder;"))
    private void renderItemCount(PonderScene scene, PonderUI screen, GuiGraphics graphics, float partialTicks,
                                 float fade, CallbackInfo ci, @Local(name = "keyWidth") int keyWidth,
                                 @Local(name = "hasIcon") boolean hasIcon) {
        if (!numismatics$showItemCount) return;

        int count = item.getCount();
        if (count == 1) return;

        Font font = screen.getFontRenderer();
        PoseStack ms = graphics.pose();
        ms.pushPose();

        String count$ = String.valueOf(count);
        int x = keyWidth + (hasIcon ? 24 : 0);
        int y = 0;

        int x$ = x + 19 - 2 - font.width(count$);
        int y$ = y + 6 + 3;

        ms.translate(0, 0, 200);
        ms.translate(x, y, 0);
        ms.scale(1.5f, 1.5f, 1.5f);
        ms.translate(x$ - x, y$ - y, 0);
        graphics.drawString(font, count$, 0, 0, 0xffffff, true);

        ms.popPose();
    }

    @ModifyReturnValue(method = "clone()Lcom/simibubi/create/foundation/ponder/element/InputWindowElement;", at = @At("RETURN"), remap = false)
    private InputWindowElement cloneShowItemCount(InputWindowElement original) {
        ((InputWindowElement_Duck) original).numismatics$showItemCount(numismatics$showItemCount);
        return original;
    }
}
