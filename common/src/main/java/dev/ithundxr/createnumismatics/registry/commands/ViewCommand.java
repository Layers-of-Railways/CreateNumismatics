package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.simibubi.create.foundation.utility.Couple;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.BankAccount;
import dev.ithundxr.createnumismatics.content.bank.Coin;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Collection;
import java.util.UUID;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ViewCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("view")
            .requires(cs -> cs.hasPermission(2))
            .then(literal("banker")
                .then(argument("pos", BlockPosArgument.blockPos())
                    .executes(ctx -> {
                        Numismatics.LOGGER.error("Banker not implemented yet.");

                        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                        UUID id = UUID.randomUUID(); // todo when banker implemented do this properly

                        return execute(ctx, id, false, "Mechanical Banker at (" + pos.toShortString() + ")");
                    })
                    .then(argument("coin", EnumArgument.enumArgument(Coin.class))
                        .executes(ctx -> {
                            Numismatics.LOGGER.error("Banker not implemented yet.");

                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                            Coin coin = ctx.getArgument("coin", Coin.class);
                            UUID id = UUID.randomUUID(); // todo when banker implemented do this properly

                            return execute(ctx, id, false, "Mechanical Banker at (" + pos.toShortString() + ")", coin);
                        })
                    )
                )
            )
            .then(argument("player", GameProfileArgument.gameProfile())
                .executes(ctx -> {
                    Collection<GameProfile> accounts = GameProfileArgument.getGameProfiles(ctx, "player");

                    int sum = 0;
                    for (GameProfile account : accounts) {
                        sum += execute(ctx, account.getId(), true, account.getName());
                    }
                    return sum;
                })
                .then(argument("coin", EnumArgument.enumArgument(Coin.class))
                    .executes(ctx -> {
                        Collection<GameProfile> accounts = GameProfileArgument.getGameProfiles(ctx, "player");
                        Coin coin = ctx.getArgument("coin", Coin.class);

                        int sum = 0;
                        for (GameProfile account : accounts) {
                            sum += execute(ctx, account.getId(), true, account.getName(), coin);
                        }
                        return sum;
                    })
                )
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, boolean create, String name) {
        return execute(ctx, account, create, name, Coin.SPUR);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, boolean create, String name, Coin coin) {
        int balance = getBalance(account, create);
        if (balance != -1) {
            Couple<Integer> coinAndRemainder = coin.convert(balance);
            int coinCount = coinAndRemainder.getFirst();
            int remainder = coinAndRemainder.getSecond();
            if (remainder == 0) {
                ctx.getSource().sendSuccess(Component.literal(name + " has " + coinCount + " "
                    + coin.getName(coinCount) + "."), true);
            } else {
                ctx.getSource().sendSuccess(Component.literal(name + " has " + coinCount + " "
                    + coin.getName(coinCount) + " and " + remainder + " " + Coin.SPUR.getName(remainder) + "."), true);
            }
            return coinCount;
        } else {
            ctx.getSource().sendFailure(Component.literal("Could not find account for "+name+"."));
            return -1;
        }
    }

    private static int getBalance(UUID id, boolean create) {
        BankAccount account = create ? Numismatics.BANK.getOrCreateAccount(id) : Numismatics.BANK.getAccount(id);
        if (account == null) {
            return -1;
        }
        return account.getBalance();
    }
}
