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

package dev.ithundxr.createnumismatics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ithundxr.createnumismatics.content.bank.BankMenu;
import dev.ithundxr.createnumismatics.content.bank.VarIntDataContainerSynchronizer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {
    @Shadow public ServerGamePacketListenerImpl connection;

    @WrapOperation(method = "initMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;setSynchronizer(Lnet/minecraft/world/inventory/ContainerSynchronizer;)V"))
    private void initMenu(AbstractContainerMenu instance, ContainerSynchronizer synchronizer, Operation<Void> original) {
        if (instance instanceof BankMenu) {
            // Bank balance can easily overflow the 16-bit short limit, so we need a custom synchronizer
            // This has to be a mixin instead of overriding BankMenu#setSynchronizer because we need the context of the player
            original.call(instance, new VarIntDataContainerSynchronizer(synchronizer, connection));
        } else {
            original.call(instance, synchronizer);
        }
    }
}
