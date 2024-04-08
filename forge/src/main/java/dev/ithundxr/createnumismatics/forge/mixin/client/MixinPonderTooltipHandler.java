package dev.ithundxr.createnumismatics.forge.mixin.client;

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
