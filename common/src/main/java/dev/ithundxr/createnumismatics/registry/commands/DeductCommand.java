package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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
                            Numismatics.LOGGER.error("Banker not implemented yet.");

                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                            int amount = getInteger(ctx, "amount");
                            UUID id = UUID.randomUUID(); // todo when banker implemented do this properly

                            return execute(ctx, id, false, "Mechanical Banker at (" + pos.toShortString() + ")", amount, force);
                        })
                        .then(argument("coin", EnumArgument.enumArgument(Coin.class))
                            .executes(ctx -> {
                                Numismatics.LOGGER.error("Banker not implemented yet.");

                                BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                                int amount = getInteger(ctx, "amount");
                                Coin coin = ctx.getArgument("coin", Coin.class);
                                UUID id = UUID.randomUUID(); // todo when banker implemented do this properly

                                return execute(ctx, id, false, "Mechanical Banker at (" + pos.toShortString() + ")", amount, force, coin);
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
                            sum += execute(ctx, account.getId(), true, account.getName(), amount, force);
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
                                sum += execute(ctx, account.getId(), true, account.getName(), amount, force, coin);
                            }
                            return sum;
                        })
                    )
                )
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, boolean create, String name, int amount, boolean force) {
        return execute(ctx, account, create, name, amount, force, Coin.SPUR);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, boolean create, String name, int amount, boolean force, Coin coin) {
        int spurValue = coin.toSpurs(amount);
        int result = deduct(account, spurValue, force, create);
        if (result == 1) {
            ctx.getSource().sendSuccess(Component.literal("Deducted "+amount+" "+coin.getName(amount)+" to "+name+"."), true);
            return spurValue;
        } else {
            if (result == -1) {
                ctx.getSource().sendFailure(Component.literal("Could not find account for "+name+"."));
            } else if (force) {
                ctx.getSource().sendSuccess(Component.literal("Force-deducted "+amount+" "+coin.getName(amount)+" from "+name+"."), true);
            } else {
                ctx.getSource().sendFailure(Component.literal("Could not deduct "+amount+" "+coin.getName(amount)+" from "+name+"."));
            }
            return result;
        }
    }

    private static int deduct(UUID id, int amount, boolean force, boolean create) {
        BankAccount account = create ? Numismatics.BANK.getOrCreateAccount(id) : Numismatics.BANK.getAccount(id);
        if (account == null) {
            return -1;
        }
        return account.deduct(amount, force) ? 1 : 0;
    }
}
