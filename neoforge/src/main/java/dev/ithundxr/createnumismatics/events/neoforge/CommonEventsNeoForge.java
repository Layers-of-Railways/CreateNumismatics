package dev.ithundxr.createnumismatics.events.neoforge;

import dev.ithundxr.createnumismatics.events.CommonEvents;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

@EventBusSubscriber
public class CommonEventsNeoForge {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ItemHandler.BLOCK,
                NumismaticsBlockEntities.VENDOR.value(),
                SidedInvWrapper::new
        );        
    }
    
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
