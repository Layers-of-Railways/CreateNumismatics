package dev.ithundxr.createnumismatics.compat.computercraft.forge;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.ComputerBehaviour;

import java.util.function.Function;

import static dev.ithundxr.createnumismatics.compat.computercraft.ComputerCraftProxy.fallbackFactory;
import static dev.ithundxr.createnumismatics.compat.computercraft.implementation.ComputerBehaviour.peripheralProvider;

public class ComputerCraftProxyImpl {

    public static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> computerFactory;

    public static void registerWithDependency() {
        /* Comment if computercraft.implementation is not in the source set */
        computerFactory = ComputerBehaviour::new;
    }
    public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
        if (computerFactory == null)
            return fallbackFactory.apply(sbe);
        return computerFactory.apply(sbe);
    }
}
