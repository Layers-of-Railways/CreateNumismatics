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

package dev.ithundxr.createnumismatics.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.multiloader.Env;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Utils {
    @ExpectPlatform
    public static boolean isDevEnv() {
        throw new AssertionError();
    }

    public static boolean testClientPlayerOrElse(Predicate<Player> predicate, boolean defaultValue) {
        MutableObject<Boolean> mutable = new MutableObject<>(defaultValue);
        Env.CLIENT.runIfCurrent(() -> () -> mutable.setValue(ClientUtils.testClientPlayer(predicate)));
        return mutable.getValue();
    }

    @ExpectPlatform
    public static void openScreen(ServerPlayer player, MenuProvider factory, Consumer<FriendlyByteBuf> extraDataWriter) {
        throw new AssertionError();
    }
}
