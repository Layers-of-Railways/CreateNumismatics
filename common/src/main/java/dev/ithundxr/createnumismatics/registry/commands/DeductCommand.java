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

package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.BankAccount.Type;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BankAccountBehaviour;
import dev.ithundxr.createnumismatics.registry.commands.arguments.EnumArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.UUID;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DeductCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        ArgumentBuilder<CommandSourceStack, ?> forceLiteral = literal("force");
        ArgumentBuilder<CommandSourceStack, ?> baseLiteral =  literal("deduct")
            .requires(cs -> cs.hasPermission(2))
            .then(register(forceLiteral, true));
        return register(baseLiteral, false);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> register(ArgumentBuilder<CommandSourceStack, ?> parent, boolean force) {
        return parent
            .then(literal("banker")
                .then(argument("pos", BlockPosArgument.blockPos())
                    .then(argument("amount", integer(0))
                        .executes(ctx -> {
                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                            int amount = getInteger(ctx, "amount");
                            BankAccountBehaviour bankAct = BlockEntityBehaviour.get(ctx.getSource().getLevel(), pos, BankAccountBehaviour.TYPE);

                            if (bankAct == null) {
                                ctx.getSource().sendFailure(Components.literal("There is no Blaze Banker at " + pos.toShortString()));
                                return -1;
                            }

                            UUID id = bankAct.getAccountUUID();

                            return execute(ctx, id, Type.BLAZE_BANKER, false, "Blaze Banker at (" + pos.toShortString() + ")", amount, force);
                        })
                        .then(argument("coin", EnumArgument.enumArgument(Coin.class))
                            .executes(ctx -> {
                                BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                int amount = getInteger(ctx, "amount");
                                Coin coin = ctx.getArgument("coin", Coin.class);
                                BankAccountBehaviour bankAct = BlockEntityBehaviour.get(ctx.getSource().getLevel(), pos, BankAccountBehaviour.TYPE);

                                if (bankAct == null) {
                                    ctx.getSource().sendFailure(Components.literal("There is no Blaze Banker at " + pos.toShortString()));
                                    return -1;
                                }

                                UUID id = bankAct.getAccountUUID();

                                return execute(ctx, id, Type.BLAZE_BANKER, false, "Blaze Banker at (" + pos.toShortString() + ")", amount, force, coin);
                            })
                        )
                    )
                )
            )
            .then(argument("player", GameProfileArgument.gameProfile())
                .then(argument("amount", integer(0))
                    .executes(ctx -> {
                        Collection<GameProfile> accounts = GameProfileArgument.getGameProfiles(ctx, "player");
                        int amount = getInteger(ctx, "amount");

                        int sum = 0;
                        for (GameProfile account : accounts) {
                            sum += execute(ctx, account.getId(), Type.PLAYER, true, account.getName(), amount, force);
                        }
                        return sum;
                    })
                    .then(argument("coin", EnumArgument.enumArgument(Coin.class))
                        .executes(ctx -> {
                            Collection<GameProfile> accounts = GameProfileArgument.getGameProfiles(ctx, "player");
                            int amount = getInteger(ctx, "amount");
                            Coin coin = ctx.getArgument("coin", Coin.class);

                            int sum = 0;
                            for (GameProfile account : accounts) {
                                sum += execute(ctx, account.getId(), Type.PLAYER, true, account.getName(), amount, force, coin);
                            }
                            return sum;
                        })
                    )
                )
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, Type type, boolean create, String name, int amount, boolean force) {
        return execute(ctx, account, type, create, name, amount, force, Coin.SPUR);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, Type type, boolean create, String name, int amount, boolean force, Coin coin) {
        int spurValue = coin.toSpurs(amount);
        int result = deduct(account, spurValue, force, create, type);
        if (result == 1) {
            ctx.getSource().sendSuccess(() -> Components.literal("Deducted "+amount+" "+coin.getName(amount)+" to "+name+"."), true);
            return spurValue;
        } else {
            if (result == -1) {
                ctx.getSource().sendFailure(Components.literal("Could not find account for "+name+"."));
            } else if (force) {
                ctx.getSource().sendSuccess(() -> Components.literal("Force-deducted "+amount+" "+coin.getName(amount)+" from "+name+"."), true);
            } else {
                ctx.getSource().sendFailure(Components.literal("Could not deduct "+amount+" "+coin.getName(amount)+" from "+name+"."));
            }
            return result;
        }
    }

    private static int deduct(UUID id, int amount, boolean force, boolean create, Type type) {
        BankAccount account = create ? Numismatics.BANK.getOrCreateAccount(id, type) : Numismatics.BANK.getAccount(id);
        if (account == null) {
            return -1;
        }
        return account.deduct(amount, force) ? 1 : 0;
    }
}
