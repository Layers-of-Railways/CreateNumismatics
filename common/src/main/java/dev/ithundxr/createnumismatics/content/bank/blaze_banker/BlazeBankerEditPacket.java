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

package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import dev.ithundxr.createnumismatics.registry.packets.BlockEntityConfigurationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class BlazeBankerEditPacket extends BlockEntityConfigurationPacket<BlazeBankerBlockEntity> {
    /*@Nullable
    private Boolean allowExtraction;*/

    @Nullable
    private String label;
    public BlazeBankerEditPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    /*public BlazeBankerEditPacket(BlockPos pos, boolean allowExtraction) {
        super(pos);
        this.allowExtraction = allowExtraction;
    }*/

    public BlazeBankerEditPacket(BlockPos pos, String label) {
        super(pos);
        this.label = label;
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {
        /*buffer.writeBoolean(allowExtraction != null);
        if (allowExtraction != null) {
            buffer.writeBoolean(allowExtraction);
            return;
        }*/

        buffer.writeBoolean(label != null);
        if (label != null) {
            buffer.writeUtf(label);
            return;
        }
    }

    @Override
    protected void readSettings(FriendlyByteBuf buf) {
        /*if (buf.readBoolean()) {
            allowExtraction = buf.readBoolean();
            return;
        }*/

        if (buf.readBoolean()) {
            label = buf.readUtf(256);
            return;
        }
    }

    @Override
    protected void applySettings(BlazeBankerBlockEntity blazeBankerBlockEntity) {
//        if (allowExtraction != null)
//            blazeBankerBlockEntity.setAllowExtraction(allowExtraction);

        if (label != null)
            blazeBankerBlockEntity.setLabel(label);
    }
}
