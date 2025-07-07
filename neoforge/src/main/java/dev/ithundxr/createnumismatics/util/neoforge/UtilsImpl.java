package dev.ithundxr.createnumismatics.util.neoforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.neoforged.fml.loading.FMLLoader;

import java.util.function.Consumer;

public class UtilsImpl {
    public static boolean isDevEnv() {
        return !FMLLoader.isProduction();
    }

    public static void openScreen(ServerPlayer player, MenuProvider factory, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        player.openMenu(factory, extraDataWriter);
    }
}
