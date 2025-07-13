package dev.ithundxr.createnumismatics.util;

import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
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
}
