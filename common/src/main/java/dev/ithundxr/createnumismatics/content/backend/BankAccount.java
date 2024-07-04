/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.NBTHelper;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.BankMenu;
import dev.ithundxr.createnumismatics.content.coins.LinkedMergingCoinBag;
import dev.ithundxr.createnumismatics.content.coins.MergingCoinBag;
import dev.ithundxr.createnumismatics.multiloader.PlayerSelection;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.packets.BankAccountLabelPacket;
import dev.ithundxr.createnumismatics.util.UsernameUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.ithundxr.createnumismatics.Numismatics.crashDev;

public class BankAccount implements MenuProvider {
    public enum Type {
        PLAYER(false, false),
        BLAZE_BANKER(true, true);

        public final boolean useTrustList;
        public final boolean hasLabel;

        Type(boolean useTrustList, boolean hasLabel) {
            this.useTrustList = useTrustList;
            this.hasLabel = hasLabel;
        }

        public static Type read(FriendlyByteBuf buf) {
            return Type.values()[buf.readInt()];
        }

        public static Type read(CompoundTag nbt) {
            String name = nbt.getString("AccountType");
            try {
                return Type.valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return PLAYER;
            }
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(ordinal());
        }

        public void write(CompoundTag nbt) {
            nbt.putString("AccountType", name());
        }
    }
    public final UUID id;
    public final Type type;
    private int balance;

    @Nullable
    private List<UUID> trustList; // only present on server

    @Nullable
    private String label;

    public final MergingCoinBag linkedCoinBag = new BankAccountCoinBag();
    private final boolean clientSide;
    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            //Numismatics.LOGGER.warn("BankAccount dataAccess#get called with index " + index + " (Account: "+BankAccount.this+"), returning "+balance);
            return balance;
        }

        @Override
        public void set(int index, int value) {
            //Numismatics.LOGGER.warn("BankAccount dataAccess#set called with index " + index + " (Account: "+BankAccount.this+"), setting balance to "+value);
            if (clientSide)
                setBalance(value);
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public BankAccount(UUID id, Type type) {
        this(id, 0, type);
    }

    public BankAccount(UUID id, int balance, Type type) {
        this(id, type, balance, false);
    }

    protected BankAccount(UUID id, Type type, int balance, boolean clientSide) {
        this.id = id;
        this.type = type;
        this.balance = balance;
        this.clientSide = clientSide;
        if (type.useTrustList && !clientSide)
            trustList = new ArrayList<>();
    }

    public static BankAccount clientSide(FriendlyByteBuf buf) {
        return new BankAccount(buf.readUUID(), Type.read(buf), buf.readVarInt(), true);
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
        return super.toString() + " {id=" + id + ", balance=" + balance + ", clientside=" + clientSide + "}";
    }

    public static BankAccount create(Type type) {
        return new BankAccount(UUID.randomUUID(), type);
    }

    public static BankAccount load(CompoundTag nbt) {
        BankAccount account;
        if (nbt.hasUUID("id")) {
            account = new BankAccount(nbt.getUUID("id"), Type.read(nbt));
        } else {
            Numismatics.LOGGER.error("Account found without ID, deleting");
            return null;
        }
        account.balance = nbt.getInt("balance");
        if (account.trustList != null && nbt.contains("TrustList")) {
            account.trustList.clear();
            account.trustList.addAll(NBTHelper.readCompoundList(
                nbt.getList("TrustList", Tag.TAG_COMPOUND),
                (tag) -> tag.getUUID("UUID")
            ));
        }
        if (account.type.hasLabel && nbt.contains("Label", Tag.TAG_STRING))
            account.label = nbt.getString("Label");
        return account;
    }

    public CompoundTag save(CompoundTag nbt) {
        nbt.putUUID("id", id);
        type.write(nbt);
        nbt.putInt("balance", balance);

        if (type.useTrustList && trustList != null) {
            trustList = trustList.stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
            nbt.put("TrustList", NBTHelper.writeCompoundList(trustList, (uuid) -> {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("UUID", uuid);
                return tag;
            }));
        }

        if (type.hasLabel && label != null)
            nbt.putString("Label", label);
        return nbt;
    }

    public void markDirty() {
        if (!clientSide)
            Numismatics.BANK.markBankDirty();
    }

    @Nullable
    public String getLabel() {
        return type.hasLabel ? label : null;
    }

    public void setLabel(@Nullable String label) {
        if (type.hasLabel) {
            if (this.label != null && this.label.equals(label))
                return;
            this.label = label;
            markDirty();
            NumismaticsPackets.PACKETS.sendTo(PlayerSelection.all(), new BankAccountLabelPacket(this));
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        if (getLabel() != null)
            return Components.literal(getLabel());

        String name = UsernameUtils.INSTANCE.getName(id, null);
        if (name != null)
            return Components.literal(name);
        return Components.translatable("block.numismatics.bank_terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return new BankMenu(NumismaticsMenuTypes.BANK.get(), i, inventory, this, dataAccess);
    }

    public void sendToMenu(FriendlyByteBuf buf) {
        buf.writeUUID(this.id);
        type.write(buf);
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
        return isAuthorized(player.getUUID());
    }

    public boolean isAuthorized(@Nullable UUID uuid) {
        if (uuid == null) return false;
        return uuid.equals(this.id) || (this.type.useTrustList && this.trustList != null && this.trustList.contains(uuid));
    }

    public void updateTrustList(Consumer<List<UUID>> updater) {
        if (trustList != null) {
            updater.accept(trustList);
            markDirty();
        }
    }
}
