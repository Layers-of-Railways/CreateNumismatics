/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

import dev.ithundxr.createnumismatics.content.backend.IGhostItemMenu;
import dev.ithundxr.createnumismatics.multiloader.C2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record GhostItemSubmitPacket(int slot, ItemStack stack) implements C2SPacket {

    public GhostItemSubmitPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readItem());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(slot);
        buffer.writeItem(stack);
    }

    @Override
    public void handle(ServerPlayer sender) {
        if (sender.containerMenu instanceof IGhostItemMenu ghostItemMenu) {
            if (ghostItemMenu.isSlotGhost(slot)) {
                ghostItemMenu.setGhostStackInSlot(slot, stack);
            }
        }
    }
}
