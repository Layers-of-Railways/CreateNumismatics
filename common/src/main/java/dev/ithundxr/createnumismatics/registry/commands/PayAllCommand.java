/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.BankAccount.Type;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.commands.arguments.EnumArgument;
import net.minecraft.commands.CommandSourceStack;

import java.util.Set;
import java.util.UUID;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PayAllCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("payall")
            .requires(cs -> cs.hasPermission(2))
            .then(argument("amount", integer(0))
                .executes(ctx -> execute(ctx, getInteger(ctx, "amount")))
                .then(argument("coin", EnumArgument.enumArgument(Coin.class))
                    .executes(ctx -> execute(ctx, getInteger(ctx, "amount"), ctx.getArgument("coin", Coin.class)))
                )
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, int amount) {
        return execute(ctx, amount, Coin.SPUR);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, int amount, Coin coin) {
        int spurValue = coin.toSpurs(amount);
        int sum = 0;

        Set<UUID> uuids = Numismatics.BANK.accounts.keySet();

        for (UUID uuid: uuids) {
            BankAccount account = Numismatics.BANK.getAccount(uuid);

            if (account != null && account.type == Type.PLAYER) {
                account.deposit(spurValue);

                sum ++;
            }
        }

        int finalSum = sum;
        ctx.getSource().sendSuccess(() -> Components.literal("Paid "+amount+" "+coin.getName(amount)+" to "+ finalSum +" account(s)."), true);

        return sum;
    }
}
