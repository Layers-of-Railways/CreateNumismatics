package dev.ithundxr.createnumismatics.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ithundxr.createnumismatics.util.ClientUtils;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GoggleOverlayRenderer.class)
public class MixinGoggleOverlayRenderer {
    @WrapOperation(
            method = "renderOverlay",
            at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/ItemEntry;asStack()Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack changeDisplayItem(ItemEntry<GogglesItem> instance, Operation<ItemStack> original) {
        return ClientUtils.changeGoggleOverlayItem(() -> original.call(instance));
    }
}
