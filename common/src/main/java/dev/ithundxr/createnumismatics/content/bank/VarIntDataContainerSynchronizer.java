/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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
