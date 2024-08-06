package dev.ithundxr.createnumismatics.content.salepoint.states.fabric;

import dev.ithundxr.createnumismatics.content.salepoint.containers.InvalidatableWrappingEnergyBuffer;
import dev.ithundxr.createnumismatics.content.salepoint.containers.fabric.InvalidatableWrappingEnergyBufferStorage;
import dev.ithundxr.createnumismatics.content.salepoint.types.EnergyBuffer;

public class EnergySalepointStateImpl {
    public static InvalidatableWrappingEnergyBuffer createBufferWrapper(EnergyBuffer buffer) {
        return new InvalidatableWrappingEnergyBufferStorage(buffer);
    }
}
