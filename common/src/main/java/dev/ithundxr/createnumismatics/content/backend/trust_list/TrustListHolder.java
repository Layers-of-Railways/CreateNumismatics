package dev.ithundxr.createnumismatics.content.backend.trust_list;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

import java.util.UUID;

public interface TrustListHolder {

    ImmutableList<UUID> getTrustList();

    /**
     * Required to have 27 slots
     */
    Container getTrustListBackingContainer();

    /**
     * Opens the trust list menu for the player
     * @param player will be checked for permission by the implementation
     */
    void openTrustListMenu(ServerPlayer player);
}
