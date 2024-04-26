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
