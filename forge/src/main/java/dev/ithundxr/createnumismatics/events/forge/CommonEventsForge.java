package dev.ithundxr.createnumismatics.events.forge;

import dev.ithundxr.createnumismatics.events.CommonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CommonEventsForge {
    @SubscribeEvent
    public static void onWorldJoin(LevelEvent.Load event) {
        CommonEvents.onLoadWorld(event.getLevel());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!CommonEvents.onBlockBreak(event.getLevel(), event.getPos(), event.getState(), event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
            CommonEvents.onPlayerJoin(serverPlayer);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        CommonEvents.onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
    }
}
