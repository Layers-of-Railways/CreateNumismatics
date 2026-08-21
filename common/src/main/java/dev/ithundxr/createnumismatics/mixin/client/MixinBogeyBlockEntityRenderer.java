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

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.bogey.BogeyBlockEntityRenderer;
import com.simibubi.create.content.trains.bogey.StandardBogeyBlockEntity;
import dev.ithundxr.createnumismatics.mixin_interfaces.StandardBogeyBlockEntity_Duck;
import dev.ithundxr.createnumismatics.ponder.utils.rendering.VirtualCouplerRendering;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BogeyBlockEntityRenderer.class)
public class MixinBogeyBlockEntityRenderer {
    @Inject(method = "renderSafe", at = @At("RETURN"))
    private <T extends BlockEntity> void numismatics$renderVirtualCoupling(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        if (be instanceof StandardBogeyBlockEntity sb && be instanceof StandardBogeyBlockEntity_Duck duck) {
            double couplingDistance = duck.numismatics$getCouplingDistance();
            if (couplingDistance > 0) {
                VirtualCouplerRendering.renderCoupler(duck.numismatics$getCouplingDirection(), couplingDistance,
                    duck.numismatics$getFront(), partialTicks, ms, buffer, light, overlay, sb);
            }
        }
    }
}
