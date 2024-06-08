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

import com.simibubi.create.foundation.blockEntity.SyncedBlockEntity;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class OpenTrustListPacket<BE extends SyncedBlockEntity & TrustListHolder> extends BlockEntityConfigurationPacket<BE> {
    public OpenTrustListPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public OpenTrustListPacket(BE be) {
        super(be.getBlockPos());
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {}

    @Override
    protected void readSettings(FriendlyByteBuf buf) {}

    @Override
    protected void applySettings(BE be) {}

    @Override
    protected void applySettings(ServerPlayer player, BE be) {
        be.openTrustListMenu(player);
    }

    @Override
    protected boolean causeUpdate() {
        return false;
    }
}
