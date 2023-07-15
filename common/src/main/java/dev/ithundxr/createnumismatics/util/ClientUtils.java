package dev.ithundxr.createnumismatics.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
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
}
