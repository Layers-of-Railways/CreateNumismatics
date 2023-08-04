package dev.ithundxr.createnumismatics.content.vendor;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListContainer;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListHolder;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VendorBlockEntity extends SmartBlockEntity implements Trusted, TrustListHolder, IHaveGoggleInformation {
    public final Container cardContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            VendorBlockEntity.this.setChanged();
        }
    };


    @Nullable
    protected UUID owner;
    protected final List<UUID> trustList = new ArrayList<>();
    public final TrustListContainer trustListContainer = new TrustListContainer(trustList, this::setChanged);

    protected final DiscreteCoinBag inventory = new DiscreteCoinBag();
    private boolean delayedDataSync = false;



    public VendorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if (owner != null)
            tag.putUUID("Owner", owner);

//        if (!inventory.isEmpty()) {
//            tag.put("Inventory", inventory.save(new CompoundTag()));
//        }

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

//        inventory.clear();
//        if (tag.contains("Inventory", Tag.TAG_COMPOUND)) {
//            inventory.load(tag.getCompound("Inventory"));
//        }

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

    public boolean isOwner(Block block, Player player) {
        if (!(block instanceof VendorBlock vendorBlock))
            return false;

        if (vendorBlock.isCreativeVendor)
            return (player != null && player.isCreative());

        return owner == null || owner.equals(player.getUUID()) || trustList.contains(player.getUUID());
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
                }
            }
        }
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

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
//        Couple<Integer> cogsAndSpurs = Coin.COG.convert(getTotalPrice());
//        int cogs = cogsAndSpurs.getFirst();
//        int spurs = cogsAndSpurs.getSecond();
//        MutableComponent balanceLabel = Components.translatable("block.numismatics.brass_depositor.tooltip.price",
//                TextUtils.formatInt(cogs), Coin.COG.getName(cogs), spurs);
//        Lang.builder()
//                .add(balanceLabel.withStyle(Coin.closest(getTotalPrice()).rarity.color))
//                .forGoggles(tooltip);
        MutableComponent label = Components.literal("Lol");
        Lang.builder().add(label).forGoggles(tooltip);
        return true;
    }
}
