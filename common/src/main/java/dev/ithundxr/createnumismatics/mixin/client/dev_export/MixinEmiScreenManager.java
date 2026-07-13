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

package dev.ithundxr.createnumismatics.mixin.client.dev_export;

import dev.ithundxr.createnumismatics.annotation.mixin.DevMixin;
import dev.ithundxr.createnumismatics.base.client.rendering.VirtualizableScreen;
import dev.ithundxr.createnumismatics.ponder.utils.dev_export.PonderExport;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@DevMixin
@Pseudo
@Mixin(targets = "dev.emi.emi.screen.EmiScreenManager")
public class MixinEmiScreenManager {
    @Inject(method = "addWidgets(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), cancellable = true)
    private static void noAddWidgets(Screen screen, CallbackInfo ci) {
        if (screen instanceof VirtualizableScreen vs && vs.isVirtual() && PonderExport.active)
            ci.cancel();
    }
}
