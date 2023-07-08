package dev.ithundxr.createnumismatics.content.backend;

import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.coins.LinkedMergingCoinBag;
import dev.ithundxr.createnumismatics.content.bank.BankMenu;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static dev.ithundxr.createnumismatics.Numismatics.crashDev;

public class BankAccount implements MenuProvider {
    public final UUID id;
    private int balance;
    public final BankAccountCoinBag linkedCoinBag = new BankAccountCoinBag();
    private final boolean clientSide;
    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return balance;
        }

        @Override
        public void set(int index, int value) {
            if (clientSide)
                setBalance(value);
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public BankAccount(UUID id) {
        this(id, 0);
    }

    public BankAccount(UUID id, int balance) {
        this(id, balance, false);
    }

    protected BankAccount(UUID id, int balance, boolean clientSide) {
        this.id = id;
        this.balance = balance;
        this.clientSide = clientSide;
    }

    public static BankAccount clientSide(FriendlyByteBuf buf) {
        return new BankAccount(buf.readUUID(), buf.readVarInt(), true);
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        if (balance == this.balance)
            return;
        if (balance < 0) {
            crashDev("Balance cannot be negative! (Account: "+this+")");
        }
        this.balance = balance;
        markDirty();
    }

    public void deposit(Coin coin, int count) {
        deposit(coin.toSpurs(count));
    }

    public void deposit(int amount) {
        if (amount < 0) {
            crashDev("Cannot add negative amount to balance! (Account: "+this+")");
            return;
        }
        setBalance(getBalance() + amount);
    }

    public boolean deduct(Coin coin, int amount) {
        return deduct(coin, amount, false);
    }

    public boolean deduct(int amount) {
        return deduct(amount, false);
    }

    public boolean deduct(Coin coin, int amount, boolean force) {
        return deduct(coin.toSpurs(amount), force);
    }

    public boolean deduct(int amount, boolean force) {
        if (amount < 0) {
            crashDev("Cannot remove negative amount from balance! (Account: "+this+")");
            return false;
        }
        if (getBalance() < amount) {
            if (force) {
                setBalance(0);
            }
            return false;
        }
        setBalance(getBalance() - amount);
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " {id=" + id + ", balance=" + balance + "}";
    }

    public static BankAccount create() {
        return new BankAccount(UUID.randomUUID());
    }

    public static BankAccount load(CompoundTag nbt) {
        BankAccount account;
        if (nbt.hasUUID("id")) {
            account = new BankAccount(nbt.getUUID("id"));
        } else {
            account = create();
        }
        account.balance = nbt.getInt("balance");
        return account;
    }

    public CompoundTag save(CompoundTag nbt) {
        nbt.putUUID("id", id);
        nbt.putInt("balance", balance);
        return nbt;
    }

    public void markDirty() {
        if (!clientSide)
            Numismatics.BANK.markBankDirty();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Components.translatable("block.numismatics.bank_terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BankMenu(NumismaticsMenuTypes.BANK.get(), i, inventory, this, dataAccess);
    }

    public void sendToMenu(FriendlyByteBuf buf) {
        buf.writeUUID(this.id);
        buf.writeVarInt(this.balance);
    }

    public boolean isClientSide() {
        return clientSide;
    }

    private class BankAccountCoinBag extends LinkedMergingCoinBag {
        @Override
        protected int getDelegate() {
            return BankAccount.this.getBalance();
        }

        @Override
        protected void setDelegate(int value) {
            BankAccount.this.setBalance(value);
        }
    }

    public boolean isAuthorized(Player player) {
        return player.getUUID().equals(this.id);
    }
}
