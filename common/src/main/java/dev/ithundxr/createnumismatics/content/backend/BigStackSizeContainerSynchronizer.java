package dev.ithundxr.createnumismatics.content.backend;

import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.BigStackSizeContainerSetContentPacket;
import dev.ithundxr.createnumismatics.registry.packets.BigStackSizeContainerSetSlotPacket;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BigStackSizeContainerSynchronizer implements ContainerSynchronizer {
    private final ServerPlayer serverPlayer;

    public BigStackSizeContainerSynchronizer(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    @Override
    public void sendInitialData(AbstractContainerMenu container, @NotNull NonNullList<ItemStack> items, @NotNull ItemStack carriedItem, int[] initialData) {
        NumismaticsPackets.PACKETS.sendTo(serverPlayer, new BigStackSizeContainerSetContentPacket(container.containerId, container.incrementStateId(), items, carriedItem));

        for (int i = 0; i < initialData.length; ++i) {
            sendDataChange(container, i, initialData[i]);
        }
    }

    @Override
    public void sendSlotChange(AbstractContainerMenu container, int slot, ItemStack itemStack) {
        NumismaticsPackets.PACKETS.sendTo(serverPlayer, new BigStackSizeContainerSetSlotPacket(container.containerId, container.incrementStateId(), slot, itemStack));
    }

    @Override
    public void sendCarriedChange(AbstractContainerMenu containerMenu, @NotNull ItemStack stack) {
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(-1, containerMenu.incrementStateId(), -1, stack));
    }

    @Override
    public void sendDataChange(AbstractContainerMenu container, int id, int value) {
        serverPlayer.connection.send(new ClientboundContainerSetDataPacket(container.containerId, id, value));
    }
}
