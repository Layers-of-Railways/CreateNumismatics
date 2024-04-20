package dev.ithundxr.createnumismatics.compat.computercraft;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.FallbackComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.compat.Mods;

import java.util.function.Function;

public class ComputerCraftProxy {

    public static void register() {
        fallbackFactory = FallbackComputerBehaviour::new;
        Mods.COMPUTERCRAFT.executeIfInstalled(() -> ComputerCraftProxy::registerWithDependency);
    }

    @ExpectPlatform
    static void registerWithDependency() {
        throw new AssertionError();
    }

    public static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> fallbackFactory;
    private static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> computerFactory;

    @ExpectPlatform
    public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
        throw new AssertionError();
    }

}