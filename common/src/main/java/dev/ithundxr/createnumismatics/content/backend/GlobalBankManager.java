package dev.ithundxr.createnumismatics.content.backend;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalBankManager {
    private BankSavedData savedData;
    public Map<UUID, BankAccount> accounts;

    public GlobalBankManager() {
        cleanUp();
    }

    private void warnIfClient() {
        if (Utils.isDevEnv() && Thread.currentThread().getName().equals("Render thread")) {
            Numismatics.LOGGER.error("Bank manager should not be accessed on the client"); // set breakpoint here when developing
        }
    }

    public void levelLoaded(LevelAccessor level) {
        MinecraftServer server = level.getServer();
        if (server == null || server.overworld() != level)
            return;
        cleanUp();
        savedData = null;
        loadBankData(server);
    }

    private void loadBankData(MinecraftServer server) {
        if (savedData != null)
            return;
        savedData = BankSavedData.load(server);
        accounts = savedData.getAccounts();
    }

    public void cleanUp() {
        accounts = new HashMap<>();
    }

    public void markBankDirty() {
        if (savedData != null)
            savedData.setDirty();
    }

    public BankAccount getAccount(Player player) {
        return getOrCreateAccount(player.getUUID());
    }

    @Nullable
    public BankAccount getAccount(UUID uuid) {
        warnIfClient();
        return accounts.get(uuid);
    }

    public BankAccount getOrCreateAccount(UUID uuid) {
        warnIfClient();
        if (accounts.containsKey(uuid)) {
            return accounts.get(uuid);
        } else {
            BankAccount account = new BankAccount(uuid);
            accounts.put(uuid, account);
            markBankDirty();
            return account;
        }
    }
}
