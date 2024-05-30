/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.simibubi.create.infrastructure.command.AllCommands;
import dev.ithundxr.createnumismatics.registry.commands.DeductCommand;
import dev.ithundxr.createnumismatics.registry.commands.PayCommand;
import dev.ithundxr.createnumismatics.registry.commands.ReloadCommandsCommand;
import dev.ithundxr.createnumismatics.registry.commands.ViewCommand;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.commands.CommandSourceStack;

import java.util.Collections;

import static net.minecraft.commands.Commands.literal;

public class NumismaticsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        var numismaticsCommand = literal("numismatics")
            .requires(cs -> cs.hasPermission(0))
            .then(PayCommand.register())
            .then(DeductCommand.register())
            .then(ViewCommand.register())
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
