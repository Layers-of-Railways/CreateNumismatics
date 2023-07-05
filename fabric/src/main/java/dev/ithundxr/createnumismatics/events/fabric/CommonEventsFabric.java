package dev.ithundxr.createnumismatics.events.fabric;

import dev.ithundxr.createnumismatics.events.CommonEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

public class CommonEventsFabric {
    public static void init() {
        ServerWorldEvents.LOAD.register((server, level) -> CommonEvents.onLoadWorld(level));
    }
}
