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

package dev.ithundxr.createnumismatics.content.salepoint.types;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class Energy {
    private long amount;

    public Energy() {
        this(0);
    }

    public Energy(long amount) {
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    public Energy setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("amount", amount);
        return tag;
    }

    public void read(CompoundTag tag) {
        amount = tag.getLong("amount");
    }

    public Energy copy() {
        return new Energy(amount);
    }

    public static Energy readFromPacket(FriendlyByteBuf buf) {
        return new Energy(buf.readLong());
    }

    public void writeToPacket(FriendlyByteBuf buf) {
        buf.writeLong(amount);
    }
}
