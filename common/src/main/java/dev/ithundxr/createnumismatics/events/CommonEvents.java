package dev.ithundxr.createnumismatics.events;

import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.world.level.LevelAccessor;

public class CommonEvents {
    public static void onLoadWorld(LevelAccessor world) {
        Numismatics.BANK.levelLoaded(world);
    }
}
