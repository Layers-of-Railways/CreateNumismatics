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

package dev.ithundxr.createnumismatics.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.ponder.PonderWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeltRenderer.class)
public abstract class MixinBeltRenderer extends SafeBlockEntityRenderer<BeltBlockEntity> {
    // TODO - Remove when https://github.com/Creators-of-Create/Create/pull/6898 is merged and released
    @ModifyExpressionValue(
            method = "renderItems",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/belt/BeltRenderer;shouldCullItem(Lnet/minecraft/world/phys/Vec3;)Z"
            ),
            require = 0
    )
    private boolean numismatics$fixBeltsCullingItemsInPonders(boolean original, @Local(argsOnly = true) BeltBlockEntity be) {
        return original && !(be.getLevel() instanceof PonderWorld);
    }
}
