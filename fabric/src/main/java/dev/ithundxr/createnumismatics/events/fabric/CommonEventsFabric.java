package dev.ithundxr.createnumismatics.events.fabric;

import dev.ithundxr.createnumismatics.events.CommonEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class CommonEventsFabric {
    public static void init() {
        ServerWorldEvents.LOAD.register((server, level) -> CommonEvents.onLoadWorld(level));
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) -> CommonEvents.onBlockBreak(level, pos, state, player));
    }
}
