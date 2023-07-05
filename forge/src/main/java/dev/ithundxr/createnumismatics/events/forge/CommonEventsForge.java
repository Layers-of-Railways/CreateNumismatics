package dev.ithundxr.createnumismatics.events.forge;

import dev.ithundxr.createnumismatics.events.CommonEvents;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommonEventsForge {
    @SubscribeEvent
    public static void onWorldJoin(LevelEvent.Load event) {
        CommonEvents.onLoadWorld(event.getLevel());
    }
}
