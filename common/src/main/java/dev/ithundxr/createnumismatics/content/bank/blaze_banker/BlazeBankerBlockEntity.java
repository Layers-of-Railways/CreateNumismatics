package dev.ithundxr.createnumismatics.content.bank.blaze_banker;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.BankAccount.Type;
import dev.ithundxr.createnumismatics.content.backend.GlobalBankManager;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListContainer;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListHolder;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlazeBankerBlockEntity extends SmartBlockEntity implements Trusted, TrustListHolder, MenuProvider {

    @Override
    public void initialize() {
        super.initialize();
        if (accountUUID == null) {
            accountUUID = UUID.randomUUID();
            notifyUpdate();
        }
    }

    protected UUID accountUUID;

    @Nullable
    protected UUID owner;

    public String getLabel() {
        if (label != null && label.isEmpty())
            label = null;
        return label;
    }

    public void setLabel(String label) {
        if (label.isEmpty())
            label = null;
        if (!level.isClientSide) {
            getAccount().setLabel(label);
        }
        this.label = label;
        notifyUpdate();
    }

    @Nullable
    protected String label;

    protected final List<UUID> trustList = new ArrayList<>();

    public final TrustListContainer trustListContainer = new TrustListContainer(trustList, this::onTrustListChanged);

    private boolean delayedDataSync = false;

    public BlazeBankerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    private void onTrustListChanged() {
        if (level == null || level.isClientSide)
            return;
        BankAccount account = getAccount();
        account.updateTrustList((accountTrustList) -> {
            accountTrustList.clear();
            accountTrustList.add(owner);
            accountTrustList.addAll(trustList);
        });
        notifyUpdate();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide)
            return;
        if (owner != null && !getAccount().isAuthorized(owner)) {
            onTrustListChanged();
        }
        if (delayedDataSync) {
            delayedDataSync = false;
            sendData();
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (owner != null)
            tag.putUUID("Owner", owner);

        if (!trustListContainer.isEmpty()) {
            tag.put("TrustListInv", trustListContainer.save(new CompoundTag()));
        }
        if (accountUUID != null)
            tag.putUUID("AccountUUID", accountUUID);
        if (getLabel() != null)
            tag.putString("Label", getLabel());
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;

        trustListContainer.clearContent();
        trustList.clear();
        if (tag.contains("TrustListInv", Tag.TAG_COMPOUND)) {
            trustListContainer.load(tag.getCompound("TrustListInv"));
        }

        if (tag.hasUUID("AccountUUID")) {
            accountUUID = tag.getUUID("AccountUUID");
        } else if (!clientPacket) {
            accountUUID = UUID.randomUUID();
        }

        if (!clientPacket && level != null && !level.isClientSide) {
            Numismatics.BANK.getOrCreateAccount(accountUUID, Type.BLAZE_BANKER);
        }

        setLabel(tag.getString("Label"));
    }

    public BankAccount getAccount() {
        if (accountUUID == null) {
            accountUUID = UUID.randomUUID();
            notifyUpdate();
        }
        return Numismatics.BANK.getOrCreateAccount(accountUUID, Type.BLAZE_BANKER);
    }

    @Override
    public boolean isTrustedInternal(Player player) {
        if (Utils.isDevEnv()) { // easier to test this way in dev
            return player.getItemBySlot(EquipmentSlot.FEET).is(Items.GOLDEN_BOOTS);
        } else {
            return owner == null || owner.equals(player.getUUID()) || trustList.contains(player.getUUID());
        }
    }

    @Override
    public ImmutableList<UUID> getTrustList() {
        return ImmutableList.copyOf(trustList);
    }

    @Override
    public Container getTrustListBackingContainer() {
        return trustListContainer;
    }

    void notifyDelayedDataSync() {
        delayedDataSync = true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Components.translatable("block.numismatics.blaze_banker");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BlazeBankerMenu(NumismaticsMenuTypes.BLAZE_BANKER.get(), i, inventory, this);
    }

    @Override
    public void destroy() {
        super.destroy();
        Numismatics.BANK.accounts.remove(accountUUID);
        Numismatics.BANK.markBankDirty();
    }

    @NotNull
    public String getLabelNonNull() {
        return getLabel() == null ? "Blaze Banker" : getLabel();
    }
}
