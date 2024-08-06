package dev.ithundxr.createnumismatics.content.salepoint.states.fabric;

import dev.ithundxr.createnumismatics.compat.Mods;
import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableWrappingEnergyBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.types.EnergyBuffer;

public class EnergySalepointStateImpl {
    public static InvalidatableWrappingEnergyBuffer createBufferWrapper(EnergyBuffer buffer) {
        return Mods.CREATEADDITION.runIfInstalled(
            () -> () -> CCACompat.createBufferWrapper(buffer)
        ).orElseGet(() -> new InvalidatableWrappingEnergyBuffer(buffer));
    }
}
