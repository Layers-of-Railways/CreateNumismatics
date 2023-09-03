package dev.ithundxr.createnumismatics.mixin.fabric.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScrollValueRenderer.class)
public class ScrollValueRendererMixin {
    @SuppressWarnings("unused")
    @WrapOperation(method = "addBox", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/utility/AdventureUtil;isAdventure(Lnet/minecraft/world/entity/player/Player;)Z"))
    private static boolean unborkGoggleOverlay(Player player, Operation<Boolean> original) {
        return true;
    }
}
