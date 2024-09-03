/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.content.salepoint;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.compat.computercraft.ComputerCraftProxy;
import dev.ithundxr.createnumismatics.config.NumismaticsConfig;
import dev.ithundxr.createnumismatics.content.backend.*;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceBehaviour;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListContainer;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListHolder;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListMenu;
import dev.ithundxr.createnumismatics.content.bank.AuthorizedCardItem;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import dev.ithundxr.createnumismatics.content.salepoint.Transaction.TransactionResult;
import dev.ithundxr.createnumismatics.content.salepoint.states.ISalepointState;
import dev.ithundxr.createnumismatics.content.salepoint.states.SalepointTypes;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.registry.packets.OpenTrustListPacket;
import dev.ithundxr.createnumismatics.util.TextUtils;
import dev.ithundxr.createnumismatics.util.UsernameUtils;
import dev.ithundxr.createnumismatics.util.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SalepointBlockEntity extends SmartBlockEntity implements Trusted, TrustListHolder, IHaveHoveringInformation {

    public final Container cardContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            SalepointBlockEntity.this.setChanged();
        }
    };

    @Nullable
    protected UUID owner;
    protected final List<UUID> trustList = new ArrayList<>();
    public final TrustListContainer trustListContainer = new TrustListContainer(trustList, this::setChanged);

    protected final DiscreteCoinBag inventory = new DiscreteCoinBag();
    protected final MenuProvider configMenuProvider = new ConfigMenuProvider();
    protected final MenuProvider purchaseMenuProvider = new PurchaseMenuProvider();
    private boolean delayedDataSync = false;
    private int lastAnalogOutput = -1;

    private SliderStylePriceBehaviour price;

    private int soundCooldown = 0;
    private boolean transactionProgressDenyPlayed = false;

    /** Must be set on placement, and SHOULDN'T be null, but just in case still check */
    @Nullable
    protected SalepointStateWrapper salepointState;

    /** Can not be serialized, because IDeductable is not serializable, this is OK */
    @Nullable
    private Transaction<?> transaction;

    @Nullable Integer justCompletedMultiplier;

    int clientsideMultiplier;
    int clientsideProgress;

    private final List<Component> clientsideTooltip = new ArrayList<>();

    AbstractComputerBehaviour computerBehaviour;

    public SalepointBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        price = new SliderStylePriceBehaviour(this, this::addCoin, this::getCoinAmount);
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
        behaviours.add(price);
    }

    @ApiStatus.Internal
    public @Nullable ISalepointState<?> getSalepointState() {
        if (salepointState == null)
            return null;
        return salepointState.state();
    }

    public @Nullable Transaction<?> getTransaction() {
        if (transaction == null)
            return null;

        if (salepointState == null) {
            transaction = null;
            notifyUpdate();
            return null;
        }

        if (!salepointState.state().filterMatchesObject(transaction.object()) || transaction.totalPrice() != getTotalPrice()) {
            transaction = null;
            notifyUpdate();
        }
        return transaction;
    }

    public boolean startTransaction(IAuthorizationCheckingDeductable deductable, int multiplier) {
        if (salepointState == null)
            return false;
        if (transaction != null)
            return false;
        if (!salepointState.state().isValidForPurchase(level, getTargetedPos(), ReasonHolder.IGNORED))
            return false;
        transaction = new Transaction<>(deductable, multiplier, salepointState.state.getFilter(), getTotalPrice());
        notifyUpdate();
        return true;
    }

    public void cancelTransaction() {
        transaction = null;
        notifyUpdate();
    }

    public @Nullable BlockPos getTargetedPos() {
        if (salepointState == null)
            return null;

        return getBlockPos().offset(salepointState.offset());
    }

    public int getCoinAmount(Coin coin) {
        return this.inventory.getDiscrete(coin);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (owner != null)
            tag.putUUID("Owner", owner);

        if (!inventory.isEmpty())
            tag.put("CoinInventory", inventory.save(new CompoundTag()));

        if (!cardContainer.getItem(0).isEmpty())
            tag.put("Card", cardContainer.getItem(0).save(new CompoundTag()));

        if (!trustListContainer.isEmpty())
            tag.put("TrustListInv", trustListContainer.save(new CompoundTag()));

        // this does have to be checked because the block place process includes serializing NBT, merging, and re-loading
        if (salepointState != null)
            tag.put("SalepointState", salepointState.serialize());

        if (clientPacket) {
            if (salepointState != null && transaction != null) {
                tag.putInt("ClientProgress", transaction.progress());
                tag.putInt("ClientMultiplier", transaction.multiplier());
            }

            if (justCompletedMultiplier != null) {
                tag.putInt("JustCompletedMultiplier", justCompletedMultiplier);
            }

            ListTag tooltip = new ListTag();
            for (Component component : clientsideTooltip) {
                String json = Component.Serializer.toJson(component);
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("Text", json);
                tooltip.add(compoundTag);
            }
            tag.put("Tooltip", tooltip);
        }
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;

        inventory.clear();
        if (tag.contains("CoinInventory", Tag.TAG_COMPOUND))
            inventory.load(tag.getCompound("CoinInventory"));

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

        if (tag.contains("SalepointState", Tag.TAG_COMPOUND))
            salepointState = SalepointStateWrapper.deserialize(tag.getCompound("SalepointState"));
        onSalepointStateSet();

        if (clientPacket) {
            clientsideProgress = tag.getInt("ClientProgress");
            clientsideMultiplier = tag.getInt("ClientMultiplier");
            justCompletedMultiplier = tag.contains("JustCompletedMultiplier", Tag.TAG_INT)
                ? tag.getInt("JustCompletedMultiplier")
                : null;

            clientsideTooltip.clear();
            ListTag tooltip = tag.getList("Tooltip", Tag.TAG_COMPOUND);
            for (Tag t : tooltip) {
                CompoundTag compoundTag = (CompoundTag) t;
                String json = compoundTag.getString("Text");
                clientsideTooltip.add(Component.Serializer.fromJson(json));
            }
        }
    }

    protected void onSalepointStateSet() {
        if (salepointState != null)
            salepointState.state().setChangedCallback(this::notifyUpdate);
    }

    @Override
    public boolean isTrustedInternal(Player player) {
        if (Utils.isDevEnv()) { // easier to test this way in dev
            return player.getItemBySlot(EquipmentSlot.FEET).is(Items.GOLDEN_BOOTS);
        } else {
            return owner == null || owner.equals(player.getUUID()) || trustList.contains(player.getUUID());
        }
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

    /**
     * NOTE: this account is ONLY for deposits
     */
    @Nullable
    public UUID getDepositAccount() {
        ItemStack cardStack = cardContainer.getItem(0);
        if (cardStack.isEmpty())
            return null;

        if (NumismaticsTags.AllItemTags.CARDS.matches(cardStack)) {
            return CardItem.get(cardStack);
        } else if (NumismaticsTags.AllItemTags.AUTHORIZED_CARDS.matches(cardStack)) {
            AuthorizedCardItem.AuthorizationPair authorizationPair = AuthorizedCardItem.get(cardStack);
            return authorizationPair == null ? null : authorizationPair.accountID();
        } else {
            return null;
        }
    }

    private void ding(AllSoundEvents.SoundEntry sound, float volume, float pitch) {
        if (soundCooldown > 0)
            return;

        sound.playOnServer(level, getBlockPos(), volume, pitch);
        soundCooldown = 10;
    }

    private void attemptTransaction() {
        Transaction<?> transaction = getTransaction();
        if (salepointState == null || transaction == null)
            return;

        TransactionResult transactionResult = transaction.doTransaction(true);
        if (transactionResult.shouldStop) {
            this.transaction = null;
            notifyUpdate();
            ding(transactionResult.sound, transactionResult.volume, transactionResult.pitch);
            return;
        }

        ISalepointState<?> state = salepointState.state();
        //noinspection DataFlowIssue
        if (!state.doPurchase(level, getTargetedPos(), ReasonHolder.IGNORED)) {
            if (!transactionProgressDenyPlayed) {
                transactionProgressDenyPlayed = true;
                ding(TransactionResult.FAILURE.sound, TransactionResult.FAILURE.volume, TransactionResult.FAILURE.pitch);
            }
            return;
        }

        transactionResult = transaction.doTransaction(false, price::addCoinsToSelf);
        if (transactionResult.shouldStop) {
            if (transactionResult == TransactionResult.SUCCESS) {
                justCompletedMultiplier = transaction.multiplier();
                ((SalepointBlock) getBlockState().getBlock()).startSignal(level, getBlockPos());
            }
            this.transaction = null;
        }

        ding(transactionResult.sound, transactionResult.volume, transactionResult.pitch);
        notifyUpdate();
    }

    int getTargetAnalogOutput() {
        if (transaction == null)
            return 0;

        return Math.max(1, Math.min(transaction.progress() * 15 / transaction.multiplier(), 15));
    }

    @Override
    public void tick() {
        super.tick();

        if (soundCooldown > 0)
            soundCooldown--;

        if (level == null || level.isClientSide)
            return;

        int analogOutput = getTargetAnalogOutput();
        if (analogOutput != lastAnalogOutput) {
            lastAnalogOutput = analogOutput;
            level.updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
        }
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void lazyTick() {
        super.lazyTick();

        if (level == null || level.isClientSide)
            return;

        if (salepointState != null) {
            salepointState.state().keepAlive();
            if (getLevel() != null)
                salepointState.state().ensureUnderControl(getLevel(), getTargetedPos());
        }

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

        justCompletedMultiplier = null;
        attemptTransaction();
        createTooltip();
    }

    void notifyDelayedDataSync() {
        delayedDataSync = true;
    }

    @Override
    public ImmutableList<UUID> getTrustList() {
        return ImmutableList.copyOf(trustList);
    }

    @Override
    public Container getTrustListBackingContainer() {
        return trustListContainer;
    }

    public void dropContents(Level level, BlockPos pos) {
        //Containers.dropContents(level, pos, this);
        Containers.dropContents(level, pos, cardContainer);
        //Containers.dropContents(level, pos, filterContainer);
        inventory.dropContents(level, pos);
    }

    @Override
    public void openTrustListMenu(ServerPlayer player) {
        TrustListMenu.openMenu(this, player, NumismaticsBlocks.SALEPOINT.asStack());
    }

    @Environment(EnvType.CLIENT)
    public void openTrustList() {
        if (level == null || !level.isClientSide)
            return;
        NumismaticsPackets.PACKETS.send(new OpenTrustListPacket<>(this));
    }

    public int getTotalPrice() {
        return price.getTotalPrice();
    }

    public int getPrice(Coin coin) {
        return price.getPrice(coin);
    }

    public void setPrice(Coin coin, int price) {
        this.price.setPrice(coin, price);
    }

    public void disableClientPriceRead() {
        price.disableClientRead();
    }

    @Override
    public void remove() {
        super.remove();
        if (salepointState != null)
            salepointState.state().onUnload();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (salepointState != null)
            salepointState.state().onUnload();
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void destroy() {
        super.destroy();
        if (salepointState != null) {
            salepointState.state().onDestroy(getLevel(), getBlockPos());
            salepointState.state().relinquishControl(getLevel(), getTargetedPos());
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (getSalepointState() == null)
            return false;

        tooltip.addAll(clientsideTooltip);

        return true;
    }

    private void createTooltip() {
        clientsideTooltip.clear();
        ISalepointState<?> state = getSalepointState();
        if (state == null)
            return;

        ReasonHolder reasonHolder = new ReasonHolder();
        if (!state.isValidForPurchase(getLevel(), getTargetedPos(), reasonHolder)) {
            Lang.builder()
                .add(reasonHolder.getMessageOrDefault(Components.translatable("gui.numismatics.salepoint.invalid_state")))
                .style(ChatFormatting.DARK_RED)
                .forGoggles(clientsideTooltip);

            boolean showOwner = reasonHolder.getMessage() == null
                || !(reasonHolder.getMessage().getContents() instanceof TranslatableContents translatableContents)
                || !translatableContents.getKey().equals("gui.numismatics.salepoint.insufficient_space");

            if (showOwner) {
                String ownerName = UsernameUtils.INSTANCE.getName(owner, null);
                if (ownerName != null) {
                    Lang.builder()
                        .add(Components.translatable("gui.numismatics.vendor.generic_named", ownerName))
                        .style(ChatFormatting.DARK_RED)
                        .forGoggles(clientsideTooltip);
                }
            }
        }

        Coin referenceCoin = NumismaticsConfig.common().referenceCoin.get();
        Couple<Integer> referenceAndSpurs = referenceCoin.convert(getTotalPrice());
        int reference = referenceAndSpurs.getFirst();
        int spurs = referenceAndSpurs.getSecond();
        MutableComponent balanceLabel = Components.translatable("gui.numismatics.salepoint.price",
            TextUtils.formatInt(reference), referenceCoin.getName(reference), spurs);

        state.createTooltip(clientsideTooltip, getLevel(), getTargetedPos());

        clientsideTooltip.add(Components.immutableEmpty());

        // For: ...

        Lang.builder()
            .add(balanceLabel.withStyle(Coin.closest(getTotalPrice()).rarity.color))
            .forGoggles(clientsideTooltip);

        for (MutableComponent component : price.getCondensedPriceBreakdown()) {
            Lang.builder()
                .add(component)
                .forGoggles(clientsideTooltip);
        }
        sendData();
    }

    public ItemStack getDisplayItem() {
        if (salepointState == null)
            return ItemStack.EMPTY;

        return salepointState.state().getDisplayItem();
    }

    private class ConfigMenuProvider implements MenuProvider {
        @Override
        public @NotNull Component getDisplayName() {
            return Components.translatable("block.numismatics.salepoint");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
            return new SalepointConfigMenu(NumismaticsMenuTypes.SALEPOINT_CONFIG.get(), i, inventory, SalepointBlockEntity.this);
        }
    }

    private class PurchaseMenuProvider implements MenuProvider {
        @Override
        public @NotNull Component getDisplayName() {
            return Components.translatable("block.numismatics.salepoint");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
            return new SalepointPurchaseMenu(NumismaticsMenuTypes.SALEPOINT_PURCHASE.get(), i, inventory, SalepointBlockEntity.this);
        }
    }

    protected record SalepointStateWrapper(@NotNull ISalepointState<?> state, @NotNull BlockPos offset) {
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.put("state", state().save());
            tag.put("pos", NbtUtils.writeBlockPos(offset()));
            return tag;
        }

        public static SalepointStateWrapper deserialize(CompoundTag tag) {
            ISalepointState<?> state = SalepointTypes.load(tag.getCompound("state"));
            BlockPos pos = NbtUtils.readBlockPos(tag.getCompound("pos"));
            return new SalepointStateWrapper(state, pos);
        }
    }
}
