package dev.ithundxr.createnumismatics.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.platform.Window;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import dev.ithundxr.createnumismatics.base.block.ForcedGoggleOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// todo Remove once https://github.com/Creators-of-Create/Create/pull/6413 is merged
@Deprecated
@Mixin(GoggleOverlayRenderer.class)
public class MixinGoggleOverlayRenderer {
    @ModifyExpressionValue(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/equipment/goggles/GogglesItem;isWearingGoggles(Lnet/minecraft/world/entity/player/Player;)Z"))
    private static boolean numismatics$isWearingTrueChange(boolean original, @Share("originalWearingGoggles") LocalBooleanRef originalWearingGoggles) {
        originalWearingGoggles.set(original);
        return true;
    }

    @WrapOperation(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/equipment/goggles/IHaveGoggleInformation;addToGoggleTooltip(Ljava/util/List;Z)Z"))
    private static boolean numismatics$alwaysWearingApiBackport(IHaveGoggleInformation instance, List<Component> tooltip, boolean isPlayerSneaking, Operation<Boolean> original, @Local(name = "be") BlockEntity be) {
        if (GogglesItem.isWearingGoggles(Minecraft.getInstance().player) || be instanceof ForcedGoggleOverlay) {
            return original.call(instance, tooltip, isPlayerSneaking);
        }
        return false;
    }

    @Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", ordinal = 0))
    private static void numismatics$resetWearingGoggles(GuiGraphics graphics, float partialTicks, Window window, CallbackInfo ci, @Local(name = "wearingGoggles") LocalBooleanRef wearingGoggles, @Share("originalWearingGoggles") LocalBooleanRef originalWearingGoggles) {
        wearingGoggles.set(originalWearingGoggles.get());
    }
}
