/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.registry.packets;

import dev.ithundxr.createnumismatics.multiloader.S2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class VarIntContainerSetDataPacket implements S2CPacket {

    private final int containerId;
    private final int id;
    private final int value;

    public VarIntContainerSetDataPacket(FriendlyByteBuf buf) {
        containerId = buf.readUnsignedByte();
        id = buf.readShort();
        value = buf.readVarInt();
    }

    public VarIntContainerSetDataPacket(int containerId, int id, int value) {
        this.containerId = containerId;
        this.id = id;
        this.value = value;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(containerId);
        buffer.writeShort(id);
        buffer.writeVarInt(value);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Minecraft mc) {
        Player player = mc.player;
        // IntelliJ falsely things that player.containerMenu is never null
        //noinspection ConstantValue,DataFlowIssue
        if (player.containerMenu != null && player.containerMenu.containerId == containerId) {
            player.containerMenu.setData(id, value);
        }
    }
}
