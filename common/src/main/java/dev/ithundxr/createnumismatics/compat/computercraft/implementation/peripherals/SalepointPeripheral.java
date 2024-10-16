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
import com.simibubi.create.foundation.utility.Components;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.*;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.Authorization;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.SubAccount;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointBlockEntity;
import dev.ithundxr.createnumismatics.content.salepoint.Transaction;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.ithundxr.createnumismatics.content.backend.Coin.getCoinFromName;
import static dev.ithundxr.createnumismatics.content.backend.Coin.getCoinsFromSpurAmount;

public class SalepointPeripheral extends SyncedPeripheral<SalepointBlockEntity> {
    public SalepointPeripheral(SalepointBlockEntity blockEntity) {
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

    @LuaFunction
    public final Map<String, Object> getSaleObject() throws LuaException {
        ISalepointState<?> state = blockEntity.getSalepointState();
        if (state == null)
            throw new LuaException("Salepoint is not initialized");
        return state.writeForComputerCraft();
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getTransaction() throws LuaException {
        Transaction<?> transaction = blockEntity.getTransaction();
        if (transaction == null)
            throw new LuaException("No transaction is currently active");
        return Map.of(
            "object", blockEntity.getSalepointState().writeForComputerCraft(),
            "unitPrice", transaction.totalPrice(),
            "targetCount", transaction.multiplier(),
            "currentCount", transaction.progress()
        );
    }

    @LuaFunction(mainThread = true)
    public final void startTransaction(String accountID, String authorizationID, int count) throws LuaException {
        if (count <= 0)
            throw new LuaException("Count must be at least 1");

        UUID account$, authorization$;
        try {
            account$ = UUID.fromString(accountID);
            authorization$ = UUID.fromString(authorizationID);
        } catch (IllegalArgumentException e) {
            throw new LuaException("Invalid UUID");
        }

        Authorization authorization = new Authorization.Anonymous(authorization$);
        BankAccount account = Numismatics.BANK.getAccount(account$);
        if (account == null) {
            throw new LuaException("Account not found");
        }

        ReasonHolder reasonHolder = new ReasonHolder();
        SubAccount subAccount = account.getSubAccount(authorization, reasonHolder);

        if (subAccount == null) {
            Component errorMessage = reasonHolder.getMessageOrDefault(Components.translatable("error.numismatics.authorized_card.account_not_found"));
            throw new LuaException(errorMessage.getString());
        }

        IDeductable deductable = subAccount.getDeductor(authorization);

        if (deductable == null) {
            throw new LuaException("Deductor not found");
        }

        IAuthorizationCheckingDeductable authorizationCheckingDeductable = IAuthorizationCheckingDeductable.of(deductable, authorization, subAccount);

        if (!blockEntity.startTransaction(authorizationCheckingDeductable, count))
            throw new LuaException("Failed to start transaction");
    }

    @LuaFunction(mainThread = true)
    public final void cancelTransaction() {
        blockEntity.cancelTransaction();
    }

    @Override
    public String getType() {
        return "Numismatics_Salepoint";
    }
}
