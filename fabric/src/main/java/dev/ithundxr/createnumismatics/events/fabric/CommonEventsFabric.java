package dev.ithundxr.createnumismatics.events.fabric;

import dev.ithundxr.createnumismatics.events.CommonEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class CommonEventsFabric {
    public static void init() {
        ServerWorldEvents.LOAD.register((server, level) -> CommonEvents.onLoadWorld(level));
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) -> CommonEvents.onBlockBreak(level, pos, state, player));
        ServerPlayConnectionEvents.JOIN.register((connection, packetSender, server) -> CommonEvents.onPlayerJoin(connection.player));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> CommonEvents.onUseBlock(player, world, hand, hitResult));
    }
}
