package dev.ithundxr.createnumismatics.util.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Consumer;

public class UtilsImpl {
    @SuppressWarnings("UnstableApiUsage")
    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    public static void openScreen(ServerPlayer player, MenuProvider factory, Consumer<FriendlyByteBuf> extraDataWriter) {
        NetworkHooks.openScreen(player, factory, extraDataWriter);
    }
}
