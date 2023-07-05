package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.ithundxr.createnumismatics.registry.NumismaticsCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.literal;

public class ReloadCommandsCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        return literal("reload_commands")
            .requires(cs -> cs.hasPermission(2))
            .executes(ctx -> {

                for (ServerPlayer player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                    ctx.getSource().getServer().getCommands().sendCommands(player);
                }
                NumismaticsCommands.register(dispatcher, dedicated);
                ctx.getSource().sendSuccess(Component.literal("Reloaded commands!"), true);
                return 1;
            });
    }
}
