/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.content.backend;

import dev.ithundxr.createnumismatics.registry.packets.BigStackSizeContainerSetContentPacket;
import dev.ithundxr.createnumismatics.registry.packets.BigStackSizeContainerSetSlotPacket;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

// todo vanilla handles this fine, remove
@Deprecated
public class BigStackSizeContainerSynchronizer implements ContainerSynchronizer {
    private final ServerPlayer serverPlayer;

    public BigStackSizeContainerSynchronizer(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    @Override
    public void sendInitialData(AbstractContainerMenu container, @NotNull NonNullList<ItemStack> items, @NotNull ItemStack carriedItem, int[] initialData) {
        CatnipServices.NETWORK.sendToClient(serverPlayer, new BigStackSizeContainerSetContentPacket(container.containerId, container.incrementStateId(), items, carriedItem));

        for (int i = 0; i < initialData.length; ++i) {
            sendDataChange(container, i, initialData[i]);
        }
    }

    @Override
    public void sendSlotChange(AbstractContainerMenu container, int slot, ItemStack itemStack) {
        CatnipServices.NETWORK.sendToClient(serverPlayer, new BigStackSizeContainerSetSlotPacket(container.containerId, container.incrementStateId(), slot, itemStack));
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
