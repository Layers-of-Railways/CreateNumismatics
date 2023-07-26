package dev.ithundxr.createnumismatics.mixin.client;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;

@Mixin(ModelBakery.class)
public class MixinModelBakery {
    /*
    injecting into the following:

    ```java
    private static final Set<Material> UNREFERENCED_TEXTURES = Util.make(Sets.newHashSet(), ids -> {
        ids.add(WATER_FLOW);
        ids.add(LAVA_FLOW);
        ids.add(WATER_OVERLAY);
        ids.add(FIRE_0);
        ids.add(FIRE_1);
    ```
     */
    //todo is this needed? new atlas may no longer require doing this
/*    @Inject(method = "method_24150", at = @At("HEAD"))
    private static void snr$addExtraUnreferencedTextures(HashSet<Material> ids, CallbackInfo ci) {
        for (Coin coin : Coin.values()) {
            ids.add(new Material(InventoryMenu.BLOCK_ATLAS, Numismatics.asResource("item/coin/outline/"+coin.getName())));
        }
        ids.add(new Material(InventoryMenu.BLOCK_ATLAS, Numismatics.asResource("item/coin/outline/animated")));
        ids.add(new Material(InventoryMenu.BLOCK_ATLAS, Numismatics.asResource("item/card/outline")));
    }*/
}
