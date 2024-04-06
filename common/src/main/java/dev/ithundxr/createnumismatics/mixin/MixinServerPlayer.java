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
