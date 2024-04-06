package dev.ithundxr.createnumismatics.content.bank;

import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.VarIntContainerSetDataPacket;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VarIntDataContainerSynchronizer implements ContainerSynchronizer {
    private final ContainerSynchronizer wrapped;
    private final ServerPlayerConnection connection;

    public VarIntDataContainerSynchronizer(ContainerSynchronizer wrapped, ServerPlayerConnection connection) {
        this.wrapped = wrapped;
        this.connection = connection;
    }

    @Override
    public void sendInitialData(AbstractContainerMenu container, NonNullList<ItemStack> items, ItemStack carriedItem, int[] initialData) {
        connection.send(new ClientboundContainerSetContentPacket(container.containerId, container.incrementStateId(), items, carriedItem));

        for(int i = 0; i < initialData.length; ++i) {
            this.sendDataChange(container, i, initialData[i]);
        }
    }

    @Override
    public void sendSlotChange(AbstractContainerMenu container, int slot, ItemStack itemStack) {
        wrapped.sendSlotChange(container, slot, itemStack);
    }

    @Override
    public void sendCarriedChange(AbstractContainerMenu containerMenu, ItemStack stack) {
        wrapped.sendCarriedChange(containerMenu, stack);
    }

    @Override
    public void sendDataChange(AbstractContainerMenu container, int id, int value) {
        NumismaticsPackets.PACKETS.sendTo(connection.getPlayer(), new VarIntContainerSetDataPacket(container.containerId, id, value));
    }
}
