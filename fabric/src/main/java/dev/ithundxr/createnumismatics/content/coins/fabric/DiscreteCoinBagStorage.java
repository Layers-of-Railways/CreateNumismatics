/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.content.coins.fabric;

import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class DiscreteCoinBagStorage implements SlottedStorage<ItemVariant> {
    private static final long CAPACITY = 65535;

    private final @NotNull DiscreteCoinBag.SidedStorageParams params;
    private final @NotNull DiscreteCoinBag coins;
    private final @NotNull Runnable setChanged;
    private final SingleSlotStorage<ItemVariant>[] slots;

    private final SnapshotParticipantImpl snapshotParticipant = new SnapshotParticipantImpl();

    public DiscreteCoinBagStorage(DiscreteCoinBag.StorageTarget target) {
        this(target.$discreteCoinBag$getParams(), target.$discreteCoinBag$getDiscreteCoinBag(), target::$discreteCoinBag$setChanged);
    }

    @SuppressWarnings("unchecked")
    public DiscreteCoinBagStorage(DiscreteCoinBag.@NotNull SidedStorageParams params, @NotNull DiscreteCoinBag coins, @NotNull Runnable setChanged) {
        this.params = params;
        this.coins = coins;
        this.setChanged = setChanged;
        this.slots = (SingleSlotStorage<ItemVariant>[]) new SingleSlotStorage[Coin.values().length];
        for (Coin coin : Coin.values()) {
            this.slots[coin.ordinal()] = new DiscreteCoinStorageImpl(coin);
        }
    }

    @Override
    public int getSlotCount() {
        return slots.length;
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        if (slot < 0 || slot >= slots.length)
            return null;
        return slots[slot];
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!params.allowInsertion())
            return 0;

        if (!(resource.getItem() instanceof CoinItem coinItem))
            return 0;

        snapshotParticipant.updateSnapshots(transaction);
        Coin coin = coinItem.coin;
        long current = coins.getDiscrete(coin);
        long canInsert = Math.max(0, CAPACITY - current);
        long inserted = Math.min(maxAmount, canInsert);

        coins.setDiscrete(coin, (int) (current + inserted));

        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!params.allowExtraction())
            return 0;

        if (!(resource.getItem() instanceof CoinItem coinItem))
            return 0;

        snapshotParticipant.updateSnapshots(transaction);
        Coin coin = coinItem.coin;
        long current = coins.getDiscrete(coin);
        long extracted = Math.min(current, maxAmount);

        coins.setDiscrete(coin, (int) (current - extracted));

        return extracted;
    }

    @Override
    public @NotNull Iterator<StorageView<ItemVariant>> iterator() {
        return Stream.of(slots)
            .<StorageView<ItemVariant>>map(v -> v)
            .iterator();
    }

    private class DiscreteCoinStorageImpl implements SingleSlotStorage<ItemVariant> {
        private final Coin coin;

        private DiscreteCoinStorageImpl(Coin coin) {
            this.coin = coin;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (!params.allowInsertion())
                return 0;

            if (!(resource.getItem() instanceof CoinItem coinItem && coinItem.coin == coin))
                return 0;

            snapshotParticipant.updateSnapshots(transaction);
            long current = coins.getDiscrete(coin);
            long canInsert = Math.max(0, CAPACITY - current);
            long inserted = Math.min(maxAmount, canInsert);

            coins.setDiscrete(coin, (int) (current + inserted));

            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (!params.allowExtraction())
                return 0;

            if (!(resource.getItem() instanceof CoinItem coinItem && coinItem.coin == coin))
                return 0;

            snapshotParticipant.updateSnapshots(transaction);
            long current = coins.getDiscrete(coin);
            long extracted = Math.min(current, maxAmount);

            coins.setDiscrete(coin, (int) (current - extracted));

            return extracted;
        }

        @Override
        public boolean isResourceBlank() {
            return false;
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(coin.asStack());
        }

        @Override
        public long getAmount() {
            return coins.getDiscrete(coin);
        }

        @Override
        public long getCapacity() {
            return CAPACITY;
        }
    }

    private class SnapshotParticipantImpl extends SnapshotParticipant<int[]> {
        @Override
        protected int[] createSnapshot() {
            int[] snapshot = new int[Coin.values().length];

            for (Coin coin : Coin.values()) {
                snapshot[coin.ordinal()] = coins.getDiscrete(coin);
            }

            return snapshot;
        }

        @Override
        protected void readSnapshot(int[] snapshot) {
            for (int i = 0; i < snapshot.length && i < Coin.values().length; i++) {
                coins.setDiscrete(Coin.values()[i], snapshot[i]);
            }
        }

        @Override
        protected void onFinalCommit() {
            super.onFinalCommit();
            setChanged.run();
        }
    }
}
