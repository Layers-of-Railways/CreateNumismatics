package dev.ithundxr.createnumismatics.content.backend.trust_list;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.Container;

import java.util.UUID;

public interface TrustListHolder {

    ImmutableList<UUID> getTrustList();

    /**
     * Required to have 27 slots
     */
    Container getTrustListBackingContainer();
}
