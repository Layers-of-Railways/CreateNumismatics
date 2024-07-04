/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.fabric.mixin.client;

import com.simibubi.create.foundation.ponder.PonderTooltipHandler;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PonderTooltipHandler.class)
public class MixinPonderTooltipHandler {
    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private static void noShowInVendor(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen == null && mc.level != null && mc.hitResult instanceof BlockHitResult blockHitResult) {
            if (mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof VendorBlock) {
                ci.cancel();
            }
        }
    }
}
