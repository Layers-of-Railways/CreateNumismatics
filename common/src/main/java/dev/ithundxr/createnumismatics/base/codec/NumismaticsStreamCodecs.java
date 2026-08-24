/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.base.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface NumismaticsStreamCodecs {
    StreamCodec<ByteBuf, UUID> UUID = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, java.util.UUID::getMostSignificantBits,
        ByteBufCodecs.VAR_LONG, java.util.UUID::getLeastSignificantBits,
        UUID::new
    );

    StreamCodec<FriendlyByteBuf, FriendlyByteBuf> NESTED_BUF = new StreamCodec<>() {
        @Override
        public @NotNull FriendlyByteBuf decode(@NotNull FriendlyByteBuf buffer) {
            FriendlyByteBuf value = new FriendlyByteBuf(Unpooled.buffer());
            int length = buffer.readVarInt();
            // limit copied from untrusted nbt accounting
            if (length > 2_097_152 && buffer.readableBytes() < length - 20) {
                throw new RuntimeException("nested buf has excessive size");
            }
            buffer.readBytes(value, length);
            return value;
        }

        @Override
        public void encode(@NotNull FriendlyByteBuf buffer, @NotNull FriendlyByteBuf value) {
            buffer.writeVarInt(value.readableBytes());
            buffer.writeBytes(value);
        }
    };
}
