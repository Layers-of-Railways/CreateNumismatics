package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
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

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ViewCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("view")
            .requires(cs -> cs.hasPermission(2))
            .then(literal("banker")
                .then(argument("pos", BlockPosArgument.blockPos())
                    .executes(ctx -> {
                        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                        BankAccountBehaviour bankAct = BlockEntityBehaviour.get(ctx.getSource().getLevel(), pos, BankAccountBehaviour.TYPE);

                        if (bankAct == null) {
                            ctx.getSource().sendFailure(Components.literal("There is no Blaze Banker at " + pos.toShortString()));
                            return -1;
                        }

                        UUID id = bankAct.getAccountUUID();

                        return execute(ctx, id, Type.BLAZE_BANKER, false, "Blaze Banker at (" + pos.toShortString() + ")");
                    })
                    .then(argument("coin", EnumArgument.enumArgument(Coin.class))
                        .executes(ctx -> {
                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                            Coin coin = ctx.getArgument("coin", Coin.class);
                            BankAccountBehaviour bankAct = BlockEntityBehaviour.get(ctx.getSource().getLevel(), pos, BankAccountBehaviour.TYPE);

                            if (bankAct == null) {
                                ctx.getSource().sendFailure(Components.literal("There is no Blaze Banker at " + pos.toShortString()));
                                return -1;
                            }

                            UUID id = bankAct.getAccountUUID();

                            return execute(ctx, id, Type.BLAZE_BANKER, false, "Blaze Banker at (" + pos.toShortString() + ")", coin);
                        })
                    )
                )
            )
            .then(argument("player", GameProfileArgument.gameProfile())
                .executes(ctx -> {
                    Collection<GameProfile> accounts = GameProfileArgument.getGameProfiles(ctx, "player");

                    int sum = 0;
                    for (GameProfile account : accounts) {
                        sum += execute(ctx, account.getId(), Type.PLAYER, true, account.getName());
                    }
                    return sum;
                })
                .then(argument("coin", EnumArgument.enumArgument(Coin.class))
                    .executes(ctx -> {
                        Collection<GameProfile> accounts = GameProfileArgument.getGameProfiles(ctx, "player");
                        Coin coin = ctx.getArgument("coin", Coin.class);

                        int sum = 0;
                        for (GameProfile account : accounts) {
                            sum += execute(ctx, account.getId(), Type.PLAYER, true, account.getName(), coin);
                        }
                        return sum;
                    })
                )
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, Type type, boolean create, String name) {
        return execute(ctx, account, type, create, name, Coin.SPUR);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, Type type, boolean create, String name, Coin coin) {
        int balance = getBalance(account, create, type);
        if (balance != -1) {
            Couple<Integer> coinAndRemainder = coin.convert(balance);
            int coinCount = coinAndRemainder.getFirst();
            int remainder = coinAndRemainder.getSecond();
            if (remainder == 0) {
                ctx.getSource().sendSuccess(() -> Components.literal(name + " has " + coinCount + " "
                    + coin.getName(coinCount) + "."), true);
            } else {
                ctx.getSource().sendSuccess(() -> Components.literal(name + " has " + coinCount + " "
                    + coin.getName(coinCount) + " and " + remainder + " " + Coin.SPUR.getName(remainder) + "."), true);
            }
            return coinCount;
        } else {
            ctx.getSource().sendFailure(Components.literal("Could not find account for "+name+"."));
            return -1;
        }
    }

    private static int getBalance(UUID id, boolean create, Type type) {
        BankAccount account = create ? Numismatics.BANK.getOrCreateAccount(id, type) : Numismatics.BANK.getAccount(id);
        if (account == null) {
            return -1;
        }
        return account.getBalance();
    }
}
