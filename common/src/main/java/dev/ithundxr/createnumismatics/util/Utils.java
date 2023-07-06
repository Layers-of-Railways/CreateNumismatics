package dev.ithundxr.createnumismatics.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.multiloader.Env;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableObject;

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
}
