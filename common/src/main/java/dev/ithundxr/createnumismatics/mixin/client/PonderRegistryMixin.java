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

import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderStoryBoardEntry;
import dev.ithundxr.createnumismatics.annotation.mixin.DevMixin;
import dev.ithundxr.createnumismatics.ponder.utils.NumismaticsSharedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@DevMixin
@Mixin(PonderRegistry.class)
public class PonderRegistryMixin {
    @Inject(method = "compile(Ljava/util/List;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/infrastructure/ponder/SharedText;gatherText()V"), remap = false)
    private static void numismatics$injectNumismaticsSharedText(List<PonderStoryBoardEntry> entries, CallbackInfoReturnable<List<PonderScene>> cir) {
        NumismaticsSharedText.gatherText();
    }
}
