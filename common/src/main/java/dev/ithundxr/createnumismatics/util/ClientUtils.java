package dev.ithundxr.createnumismatics.util;

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

    public static ItemStack changeGoggleOverlayItem() {
        ItemStack goggles = AllItems.GOGGLES.asStack();

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return goggles;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null)
            return goggles;

        if (level.getBlockEntity(blockHitResult.getBlockPos()) instanceof VendorBlockEntity) {
            return NumismaticsItems.getCoin(Coin.COG).asStack();
        }
        return goggles;
    }
}
