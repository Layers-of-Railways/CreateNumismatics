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

package dev.ithundxr.createnumismatics.mixin.compat.carryon;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.annotation.mixin.ConditionalMixin;
import dev.ithundxr.createnumismatics.compat.Mods;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.common.carry.PickupHandler;

import java.util.function.BiFunction;

@ConditionalMixin(mods = Mods.CARRYON)
@Mixin(PickupHandler.class)
public class MixinPickupHandler {
    @Inject(method = "tryPickUpBlock", at = @At("HEAD"), cancellable = true)
    private static void preventNumismaticsPickup(ServerPlayer player, BlockPos pos, Level level, BiFunction<BlockState, BlockPos, Boolean> pickupCallback, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(pos);
        if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals(Numismatics.MOD_ID))
            cir.setReturnValue(false);
    }
}
