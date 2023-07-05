package dev.ithundxr.createnumismatics.registry.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.BankAccount;
import dev.ithundxr.createnumismatics.content.bank.Coin;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.UUID;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PayCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("pay")
            .requires(cs -> cs.hasPermission(2))
            .then(literal("banker")
                .then(argument("pos", BlockPosArgument.blockPos()))
                .then(argument("amount", integer(0))
                .executes(ctx -> {
                    Numismatics.LOGGER.error("Banker not implemented yet.");

                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
                    int amount = getInteger(ctx, "amount");

                    return execute(ctx, UUID.randomUUID(), "Mechanical Banker at ("+pos.toShortString()+")", amount);
                }))
            )
            .then(argument("player", GameProfileArgument.gameProfile()))
            .then(argument("amount", integer(0)))
            .executes(ctx -> {
                Collection<GameProfile> accounts = GameProfileArgument.getGameProfiles(ctx, "account");
                int amount = getInteger(ctx, "amount");

                int sum = 0;
                for (GameProfile account : accounts) {
                    sum += execute(ctx, account.getId(), account.getName(), amount);
                }
                return sum;
            });
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, String name, int amount) {
        return execute(ctx, account, name, amount, Coin.SPUR);
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, UUID account, String name, int amount, Coin coin) {
        int spurValue = coin.toSpurs(amount);
        if (pay(account, spurValue)) {
            ctx.getSource().sendSuccess(Component.literal("Paid "+amount+" "+coin.getName()+" to "+name+"."), true);
            return spurValue;
        } else {
            ctx.getSource().sendFailure(Component.literal("Could not find account for "+account+"."));
            return 0;
        }
    }

    private static boolean pay(UUID id, int amount) {
        BankAccount account = Numismatics.BANK.getAccount(id);
        if (account == null) {
            return false;
        }
        account.deposit(amount);
        return true;
    }
}
