package dev.ithundxr.createnumismatics.util.fabric;

import io.github.fabricators_of_create.porting_lib.util.NetworkHooks;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

import java.util.function.Consumer;

public class UtilsImpl {
    public static boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static void openScreen(ServerPlayer player, MenuProvider factory, Consumer<FriendlyByteBuf> extraDataWriter) {
        NetworkHooks.openScreen(player, factory, extraDataWriter);
    }
}
