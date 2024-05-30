/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BitmapProvider.Definition.class)
public class MixinBitmapProvider$Definition {
    @WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/providers/BitmapProvider$Definition;getActualGlyphWidth(Lcom/mojang/blaze3d/platform/NativeImage;IIII)I"))
    private int monospaceCoins(BitmapProvider.Definition instance, NativeImage image, int width, int height, int x, int y, Operation<Integer> original) {
        if (instance.file().getNamespace().equals(Numismatics.MOD_ID) && instance.file().getPath().startsWith("item/coin/"))
            return 16;
        return original.call(instance, image, width, height, x, y);
    }
}
