package dev.ithundxr.createnumismatics.events.neoforge;

import dev.ithundxr.createnumismatics.compat.Mods;
import dev.ithundxr.createnumismatics.compat.computercraft.ComputerCraftProxy;
import dev.ithundxr.createnumismatics.events.CommonEvents;
import dev.ithundxr.createnumismatics.neoforge.capability_ducks.BrassDepositorBlockEntity_Duck;
import dev.ithundxr.createnumismatics.neoforge.capability_ducks.SalepointBlockEntity_Duck;
import dev.ithundxr.createnumismatics.neoforge.capability_ducks.VendorBlockEntity_Duck;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber
public class CommonEventsNeoForge {
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
        InteractionResult result = CommonEvents.onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        BrassDepositorBlockEntity_Duck.registerCapabilities(event);
        VendorBlockEntity_Duck.registerCapabilities(event);
        SalepointBlockEntity_Duck.registerCapabilities(event);

        if (Mods.COMPUTERCRAFT.isLoaded) {
            ComputerCraftProxy.bankTerminalPeripheralRegistrar.accept(event);
        }
    }
}
