/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.fabric.mixin;

import dev.ithundxr.createnumismatics.annotation.mixin.DevMixin;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@DevMixin
@Mixin(targets = "com.simibubi.create.AllFluids$CreateAttributeHandler")
@SuppressWarnings("UnstableApiUsage")
public class MixinAllFluids$CreateAttributeHandler {
    @Shadow @Final private Component name;

    // Make tea lighter than air to test salepoint rendering
    @Inject(method = "isLighterThanAir", at = @At("HEAD"), cancellable = true, remap = false)
    private void teaFloats(FluidVariant variant, CallbackInfoReturnable<Boolean> cir) {
        if (this.name.getContents() instanceof TranslatableContents translatableContents && translatableContents.getKey().equals("fluid.create.tea")) {
            cir.setReturnValue(true);
        }
    }
}
