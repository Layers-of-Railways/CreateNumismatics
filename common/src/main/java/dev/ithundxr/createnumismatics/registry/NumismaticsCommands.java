package dev.ithundxr.createnumismatics.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.simibubi.create.infrastructure.command.AllCommands;
import dev.ithundxr.createnumismatics.registry.commands.PayCommand;
import dev.ithundxr.createnumismatics.registry.commands.ReloadCommandsCommand;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collections;

import static net.minecraft.commands.Commands.literal;

public class NumismaticsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        var numismaticsCommand = literal("numismatics")
            .requires(cs -> cs.hasPermission(0))
            .then(PayCommand.register())
            //.then(ClearCasingCacheCommand.register())
            //.then(SplitTrainCommand.register())
            //.then(TrainInfoCommand.register());
        ;

        if (Utils.isDevEnv()) {
            numismaticsCommand = numismaticsCommand
                .then(ReloadCommandsCommand.register(dispatcher, dedicated))
            ;
        }

        LiteralCommandNode<CommandSourceStack> numismaticsRoot = dispatcher.register(numismaticsCommand);

        CommandNode<CommandSourceStack> nm = dispatcher.findNode(Collections.singleton("nm"));
        if (nm != null)
            return;

        dispatcher.getRoot()
            .addChild(AllCommands.buildRedirect("nm", numismaticsRoot));
    }
}
