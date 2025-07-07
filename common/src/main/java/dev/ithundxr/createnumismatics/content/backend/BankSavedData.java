package dev.ithundxr.createnumismatics.content.backend;

import com.simibubi.create.content.trains.RailwaySavedData;
import dev.ithundxr.createnumismatics.Numismatics;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankSavedData extends SavedData {
    private Map<UUID, BankAccount> accounts = new HashMap<>();

    public static SavedData.Factory<BankSavedData> factory() {
        return new SavedData.Factory<>(BankSavedData::new, BankSavedData::load, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }
    
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, @NotNull Provider registries) {
        tag.put("Accounts", NBTHelper.writeCompoundList(Numismatics.BANK.accounts.values(), t -> t.save(new CompoundTag())));
        return tag;
    }

    private static BankSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        BankSavedData sd = new BankSavedData();
        sd.accounts = new HashMap<>();

        NBTHelper.iterateCompoundList(tag.getList("Accounts", Tag.TAG_COMPOUND), c -> {
            BankAccount account = BankAccount.load(c);
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
