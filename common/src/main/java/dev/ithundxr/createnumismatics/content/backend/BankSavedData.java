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

package dev.ithundxr.createnumismatics.content.backend;

import com.simibubi.create.foundation.utility.NBTHelper;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankSavedData extends SavedData {
    private Map<UUID, BankAccount> accounts = new HashMap<>();

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        nbt.put("Accounts", NBTHelper.writeCompoundList(Numismatics.BANK.accounts.values(), t -> t.save(new CompoundTag())));
        return nbt;
    }

    private static BankSavedData load(CompoundTag nbt) {
        BankSavedData sd = new BankSavedData();
        sd.accounts = new HashMap<>();

        NBTHelper.iterateCompoundList(nbt.getList("Accounts", Tag.TAG_COMPOUND), c -> {
            BankAccount account = BankAccount.load(c);
            sd.accounts.put(account.id, account);
        });

        return sd;
    }

    private BankSavedData() {}

    public static BankSavedData load(MinecraftServer server) {
        return server.overworld()
            .getDataStorage()
            .computeIfAbsent(BankSavedData::load, BankSavedData::new, "numismatics_bank");
    }

    public Map<UUID, BankAccount> getAccounts() {
        return accounts;
    }
}
