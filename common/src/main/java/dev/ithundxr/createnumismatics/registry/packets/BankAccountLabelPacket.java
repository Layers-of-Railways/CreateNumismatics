/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

import dev.ithundxr.createnumismatics.NumismaticsClient;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.multiloader.S2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BankAccountLabelPacket implements S2CPacket {

    @NotNull
    private final UUID id;
    @Nullable
    private final String label;

    public BankAccountLabelPacket(FriendlyByteBuf buf) {
        id = buf.readUUID();
        label = buf.readBoolean() ? buf.readUtf(256) : null;
    }

    public BankAccountLabelPacket(BankAccount account) {
        this.id = account.id;
        this.label = account.getLabel();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(id);
        buffer.writeBoolean(label != null);
        if (label != null)
            buffer.writeUtf(label);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Minecraft mc) {
        if (label == null) {
            NumismaticsClient.bankAccountLabels.remove(id);
        } else {
            NumismaticsClient.bankAccountLabels.put(id, label);
        }
    }
}
