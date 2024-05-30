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

package dev.ithundxr.createnumismatics.content.depositor;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListContainer;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListHolder;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.registry.packets.OpenTrustListPacket;
import dev.ithundxr.createnumismatics.util.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractDepositorBlockEntity extends SmartBlockEntity implements IHaveHoveringInformation, Trusted, TrustListHolder {

    public final Container cardContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            AbstractDepositorBlockEntity.this.setChanged();
        }
    };

    @Nullable
    protected UUID owner;

    protected final List<UUID> trustList = new ArrayList<>();

    public final TrustListContainer trustListContainer = new TrustListContainer(trustList, this::setChanged);

    protected final DiscreteCoinBag inventory = new DiscreteCoinBag();
    private boolean delayedDataSync = false;

    public AbstractDepositorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable UUID getCardId() {
        ItemStack card = cardContainer.getItem(0);
        if (!(card.getItem() instanceof CardItem))
            return null;
        return CardItem.get(card);
    }

    public void activate() {
        if (level instanceof ServerLevel serverLevel) {
            if (getBlockState().getBlock() instanceof AbstractDepositorBlock<?> depositorBlock) {
                depositorBlock.activate(getBlockState(), serverLevel, worldPosition);
            }
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (owner != null)
            tag.putUUID("Owner", owner);

        if (!inventory.isEmpty()) {
            tag.put("Inventory", inventory.save(new CompoundTag()));
        }

        if (!cardContainer.getItem(0).isEmpty()) {
            tag.put("Card", cardContainer.getItem(0).save(new CompoundTag()));
        }

        if (!trustListContainer.isEmpty()) {
            tag.put("TrustListInv", trustListContainer.save(new CompoundTag()));
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;

        inventory.clear();
        if (tag.contains("Inventory", Tag.TAG_COMPOUND)) {
            inventory.load(tag.getCompound("Inventory"));
        }

        if (tag.contains("Card", Tag.TAG_COMPOUND)) {
            ItemStack cardStack = ItemStack.of(tag.getCompound("Card"));
            cardContainer.setItem(0, cardStack);
        } else {
            cardContainer.setItem(0, ItemStack.EMPTY);
        }

        trustListContainer.clearContent();
        trustList.clear();
        if (tag.contains("TrustListInv", Tag.TAG_COMPOUND)) {
            trustListContainer.load(tag.getCompound("TrustListInv"));
        }
    }

    @Override
    public boolean isTrustedInternal(Player player) {
        if (Utils.isDevEnv()) { // easier to test this way in dev
            return player.getItemBySlot(EquipmentSlot.FEET).is(Items.GOLDEN_BOOTS);
        } else {
            return owner == null || owner.equals(player.getUUID()) || trustList.contains(player.getUUID());
        }
    }

    @Nullable
    public UUID getDepositAccount() {
        ItemStack cardStack = cardContainer.getItem(0);
        if (cardStack.isEmpty())
            return null;
        if (!NumismaticsTags.AllItemTags.CARDS.matches(cardStack))
            return null;

        return CardItem.get(cardStack);
    }

    public void addCoin(Coin coin, int count) {
        UUID depositAccount = getDepositAccount();
        if (depositAccount != null) {
            BankAccount account = Numismatics.BANK.getAccount(depositAccount);
            if (account != null) {
                account.deposit(coin, count);
                return;
            }
        }
        inventory.add(coin, count);
        setChanged();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide)
            return;
        if (delayedDataSync) {
            delayedDataSync = false;
            sendData();
        }
        UUID depositAccount = getDepositAccount();
        if (depositAccount != null && !inventory.isEmpty()) {
            BankAccount account = Numismatics.BANK.getAccount(depositAccount);
            if (account != null) {
                for (Coin coin : Coin.values()) {
                    int count = inventory.getDiscrete(coin);
                    inventory.subtract(coin, count);
                    account.deposit(coin, count);
                    notifyUpdate();
                }
            }
        }
    }

    void notifyDelayedDataSync() {
        delayedDataSync = true;
    }

    @Environment(EnvType.CLIENT)
    public void openTrustList() {
        if (level == null || !level.isClientSide)
            return;
        NumismaticsPackets.PACKETS.send(new OpenTrustListPacket<>(this));
    }

    @Override
    public ImmutableList<UUID> getTrustList() {
        return ImmutableList.copyOf(trustList);
    }

    @Override
    public Container getTrustListBackingContainer() {
        return trustListContainer;
    }
}
