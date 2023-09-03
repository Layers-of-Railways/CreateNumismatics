package dev.ithundxr.createnumismatics.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ithundxr.createnumismatics.content.coins.SlotDiscreteCoinBag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public class MixinAbstractContainerMenu {
    @Inject(method = "moveItemStackTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void moveCoins(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, CallbackInfoReturnable<Boolean> cir, @Local Slot slot) {
        if (slot instanceof SlotDiscreteCoinBag slotDiscreteCoinBag && slotDiscreteCoinBag.tryPlace(stack))
            cir.setReturnValue(true); // special handling for Discrete Coin Bag, cancel default placement
    }
}
