package dev.ithundxr.createnumismatics.content.bank;

import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

import static dev.ithundxr.createnumismatics.Numismatics.crashDev;

public class BankAccount {
    public final UUID id;
    private int balance = 0;

    public BankAccount(UUID id) {
        this.id = id;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
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

    public boolean removeBalance(Coin coin, int amount) {
        return removeBalance(coin, amount, false);
    }

    public boolean removeBalance(int amount) {
        return removeBalance(amount, false);
    }

    public boolean removeBalance(Coin coin, int amount, boolean force) {
        return removeBalance(coin.toSpurs(amount), force);
    }

    public boolean removeBalance(int amount, boolean force) {
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
        Numismatics.BANK.markBankDirty();
    }
}
