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

package dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals;

import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;

import java.util.List;
import java.util.Map;

import static dev.ithundxr.createnumismatics.content.backend.Coin.getCoinFromName;
import static dev.ithundxr.createnumismatics.content.backend.Coin.getCoinsFromSpurAmount;

public class VendorPeripheral extends SyncedPeripheral<VendorBlockEntity> {
    public VendorPeripheral(VendorBlockEntity blockEntity) {
        super(blockEntity);
    }

    @LuaFunction(mainThread = true)
    public final void setCoinAmount(String coinName, int amount) throws LuaException {
        Coin coin = getCoinFromName(coinName);
        if(coin == null) throw new LuaException("incorrect coin name");
        blockEntity.setPrice(coin, amount);
        blockEntity.notifyUpdate();
    }

    @LuaFunction(mainThread = true)
    public final void setTotalPrice(int spurAmount) {
        List<Map.Entry<Coin, Integer>> coins = getCoinsFromSpurAmount(spurAmount);
        for (Map.Entry<Coin, Integer> coin : coins) {
            blockEntity.setPrice(coin.getKey(), coin.getValue());
        }
        blockEntity.notifyUpdate();
    }

    @LuaFunction
    public final int getTotalPrice() {
        return blockEntity.getTotalPrice();
    }

    @LuaFunction
    public final int getPrice(String coinName) throws LuaException {
        Coin coin = getCoinFromName(coinName);
        if(coin == null) throw new LuaException("incorrect coin name");
        return blockEntity.getPrice(coin);
    }

    @Override
    public String getType() {
        return "Numismatics_Vendor";
    }
}
