package dev.ithundxr.createnumismatics.content.backend;

import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

public interface Trusted {
    @OverrideOnly
    boolean isTrustedInternal(Player player);

    default boolean isTrusted(Player player) {
        return isForceTrusted(player) || isTrustedInternal(player);
    }

    static boolean isForceTrusted(Player player) {
        if (Utils.isDevEnv())
            return player.getItemBySlot(EquipmentSlot.LEGS).is(Items.GOLDEN_LEGGINGS);
        return player.hasPermissions(2);
    }
}
