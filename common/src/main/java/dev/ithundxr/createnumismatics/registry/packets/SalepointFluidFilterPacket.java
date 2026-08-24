/*
 * Numismatics
 * Copyright (c) 2024-2026 The Railways Team
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

package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.content.salepoint.SalepointConfigMenu;
import dev.ithundxr.createnumismatics.content.salepoint.states.FluidSalepointState;
import dev.ithundxr.createnumismatics.multiloader.fluid.MultiloaderFluidStack;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public record SalepointFluidFilterPacket(MultiloaderFluidStack filter) implements ServerboundPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SalepointFluidFilterPacket> STREAM_CODEC =
        MultiloaderFluidStack.OPTIONAL_STREAM_CODEC.map(
            SalepointFluidFilterPacket::new,
            SalepointFluidFilterPacket::filter
        );

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof SalepointConfigMenu salepointConfigMenu) {
            if (salepointConfigMenu.getSalepointState() instanceof FluidSalepointState fluidSalepointState) {
                fluidSalepointState.setFilter(filter, salepointConfigMenu.contentHolder.getLevel(), salepointConfigMenu.contentHolder.getBlockPos(), sender);
            }
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return NumismaticsPackets.SALEPOINT_FLUID_FILTER;
    }
}
