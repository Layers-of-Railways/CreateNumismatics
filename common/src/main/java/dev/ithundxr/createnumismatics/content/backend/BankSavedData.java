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
