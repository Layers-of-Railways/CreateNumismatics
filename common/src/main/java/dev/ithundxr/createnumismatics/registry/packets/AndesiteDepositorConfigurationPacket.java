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

import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;

public class AndesiteDepositorConfigurationPacket extends BlockEntityConfigurationPacket<AndesiteDepositorBlockEntity> {

    private Coin coin;

    public AndesiteDepositorConfigurationPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    public AndesiteDepositorConfigurationPacket(AndesiteDepositorBlockEntity be) {
        super(be.getBlockPos());
        this.coin = be.getCoin();
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {
        buffer.writeEnum(coin);
    }

    @Override
    protected void readSettings(FriendlyByteBuf buf) {
        coin = buf.readEnum(Coin.class);
    }

    @Override
    protected void applySettings(AndesiteDepositorBlockEntity andesiteDepositorBlockEntity) {
        andesiteDepositorBlockEntity.setCoin(coin);
    }
}
