package dev.ithundxr.createnumismatics.util;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ithundxr.createnumismatics.base.block.ForcedGoggleOverlay;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ClientUtils {
    @Environment(EnvType.CLIENT)
    public static boolean testClientPlayer(Predicate<Player> predicate) {
        return predicate.test(Minecraft.getInstance().player);
    }

    public static boolean isLookingAtForcedGoggleOverlay() {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return false;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return false;

        return level.getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof ForcedGoggleOverlay;
    }


    private static final ItemStack BARRIER_STACK = new ItemStack(Items.BARRIER);
    public static ItemStack changeGoggleOverlayItem(Supplier<ItemStack> original) {

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return original.get();

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return original.get();

        if (level.getBlockEntity(blockHitResult.getBlockPos()) instanceof VendorBlockEntity vendorBE) {
            // get the block entities cost and show the item for that and its cost and under
            // show what is being sold (the enchants)
            return vendorBE.sellingContainer.isEmpty() ? BARRIER_STACK : vendorBE.sellingContainer.getItem(0);
        }
        return original.get();
    }
}
