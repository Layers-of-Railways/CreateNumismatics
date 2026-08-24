/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
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

package dev.ithundxr.createnumismatics.content.backend;

import dev.ithundxr.createnumismatics.Numismatics;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
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

    public static SavedData.Factory<BankSavedData> factory() {
		//noinspection DataFlowIssue
		return new SavedData.Factory<>(BankSavedData::new, BankSavedData::load, null);
    }
    
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        tag.put("Accounts", NBTHelper.writeCompoundList(Numismatics.BANK.accounts.values(), t -> t.save(new CompoundTag(), registries)));
        return tag;
    }

    private static BankSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        BankSavedData sd = new BankSavedData();
        sd.accounts = new HashMap<>();

        NBTHelper.iterateCompoundList(tag.getList("Accounts", Tag.TAG_COMPOUND), c -> {
            BankAccount account = BankAccount.load(c, registries);
            if (account != null)
                sd.accounts.put(account.id, account);
        });

        return sd;
    }

    private BankSavedData() {}

    public static BankSavedData load(MinecraftServer server) {
        return server.overworld()
            .getDataStorage()
            .computeIfAbsent(factory(), "numismatics_bank");
    }

    public Map<UUID, BankAccount> getAccounts() {
        return accounts;
    }
}
