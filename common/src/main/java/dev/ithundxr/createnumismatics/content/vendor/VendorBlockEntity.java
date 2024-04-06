package dev.ithundxr.createnumismatics.content.vendor;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.Trusted;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceBehaviour;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListContainer;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListHolder;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListMenu;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.registry.NumismaticsPackets;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.registry.packets.OpenTrustListPacket;
import dev.ithundxr.createnumismatics.util.ItemUtil;
import dev.ithundxr.createnumismatics.util.TextUtils;
import dev.ithundxr.createnumismatics.util.UsernameUtils;
import dev.ithundxr.createnumismatics.util.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VendorBlockEntity extends SmartBlockEntity implements Trusted, TrustListHolder, IHaveGoggleInformation, WorldlyContainer, MenuProvider {
    public final Container cardContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            VendorBlockEntity.this.setChanged();
        }
    };

    public final Container sellingContainer = new SimpleContainer(1) {
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

    private SliderStylePriceBehaviour price;
    private Mode mode = Mode.SELL;
    public final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);


    public VendorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        price = new SliderStylePriceBehaviour(this, this::addCoin, this::getCoinCount);
        behaviours.add(price);
    }

    public int getCoinCount(Coin coin) {
        return this.inventory.getDiscrete(coin);
    }

    public @Nullable UUID getCardId() {
        ItemStack card = cardContainer.getItem(0);
        if (!(card.getItem() instanceof CardItem))
            return null;
        return CardItem.get(card);
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

        if (!getSellingItem().isEmpty()) {
            tag.put("Selling", getSellingItem().save(new CompoundTag()));
        }

        if (!trustListContainer.isEmpty()) {
            tag.put("TrustListInv", trustListContainer.save(new CompoundTag()));
        }

        if (!items.isEmpty()) {
            tag.put("Inventory", ContainerHelper.saveAllItems(new CompoundTag(), items));
        }

        tag.putInt("Mode", mode.ordinal());
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

        if (tag.contains("Selling", Tag.TAG_COMPOUND)) {
            ItemStack sellingStack = ItemStack.of(tag.getCompound("Selling"));
            sellingContainer.setItem(0, sellingStack);
        } else {
            sellingContainer.setItem(0, ItemStack.EMPTY);
        }

        trustListContainer.clearContent();
        trustList.clear();
        if (tag.contains("TrustListInv", Tag.TAG_COMPOUND)) {
            trustListContainer.load(tag.getCompound("TrustListInv"));
        }

        items.clear();
        if (tag.contains("Inventory", Tag.TAG_COMPOUND)) {
            ContainerHelper.loadAllItems(tag.getCompound("Inventory"), items);
        }

        mode = Mode.values()[tag.getInt("Mode")];
    }

    @Nullable
    private Boolean isCreativeVendorCached;

    public boolean isCreativeVendor() {
        if (isCreativeVendorCached == null)
            isCreativeVendorCached = getBlockState().getBlock() instanceof VendorBlock vendorBlock && vendorBlock.isCreativeVendor;
        return isCreativeVendorCached;
    }

    @Override
    public boolean isTrustedInternal(Player player) {
        if (Utils.isDevEnv()) { // easier to test this way in dev
            return player.getItemBySlot(EquipmentSlot.FEET).is(Items.GOLDEN_BOOTS);
        } else {
            if (isCreativeVendor()) {
                return player != null && player.isCreative();
            }

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
    @Environment(EnvType.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        ItemStack sellingStack = getSellingItem();
        if (sellingStack.isEmpty())
            return false;

        Couple<Integer> cogsAndSpurs = Coin.COG.convert(getTotalPrice());
        int cogs = cogsAndSpurs.getFirst();
        int spurs = cogsAndSpurs.getSecond();
        MutableComponent balanceLabel = Components.translatable("block.numismatics.vendor.tooltip.price",
            TextUtils.formatInt(cogs), Coin.COG.getName(cogs), spurs);

        // Selling/Buying
        Lang.builder()
            .add(Components.translatable(mode.getOpposite().getTranslationKey()))
            .forGoggles(tooltip);

        // Item
        // ...
        boolean isFirst = true;
        for (Component component : Screen.getTooltipFromItem(Minecraft.getInstance(), sellingStack)) {
            MutableComponent mutable = component.copy();
            if (isFirst) {
                isFirst = false;
                if (sellingStack.getCount() != 1) {
                    mutable.append(
                        Components.translatable("gui.numismatics.vendor.count", sellingStack.getCount())
                            .withStyle(ChatFormatting.GREEN)
                    );
                }
            }
            Lang.builder()
                .add(mutable)
                .forGoggles(tooltip);
        }

        tooltip.add(Components.immutableEmpty());

        // For: ...

        Lang.builder()
            .add(balanceLabel.withStyle(Coin.closest(getTotalPrice()).rarity.color))
            .forGoggles(tooltip);
        return true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Components.translatable(isCreativeVendor() ? "block.numismatics.creative_vendor" : "block.numismatics.vendor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        if (!isTrusted(player))
            return null;
        return new VendorMenu(NumismaticsMenuTypes.VENDOR.get(), i, inventory, this);
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

    /**
     * Note: if mode == BUY, then this is actually the buying item - if someone has better names, please refactor :)
     */
    public ItemStack getSellingItem() {
        return sellingContainer.getItem(0);
    }

    /* Begin Container */

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        if (mode == Mode.BUY)
            return side == Direction.DOWN ? new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8} : new int[0];
        return side == Direction.DOWN ? new int[0] : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        return mode == Mode.SELL && matchesSellingItem(stack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return direction != Direction.DOWN && canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return direction == Direction.DOWN && mode == Mode.BUY;
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(items, slot, amount);
        if (!itemStack.isEmpty()) {
            setChanged();
        }

        return itemStack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack itemStack = items.get(slot);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            items.set(slot, ItemStack.EMPTY);
            return itemStack;
        }
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

        setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    /* End Container */

    @NotNull
    @Contract("_ -> new")
    private CompoundTag cleanTags(@NotNull CompoundTag tag) {
        tag = tag.copy();
        tag.remove("RepairCost");
        tag.remove("Count");

        // sort enchants
        ListTag enchants = tag.getList("Enchantments", Tag.TAG_COMPOUND);
        if (!enchants.isEmpty()) {
            ArrayList<Tag> tags = new ArrayList<>(enchants);
            tags.sort((a, b) -> {
                if (a.equals(b))
                    return 0;
                if (a instanceof CompoundTag ca && b instanceof CompoundTag cb) {
                    if (ca.contains("id", Tag.TAG_STRING) && cb.contains("id", Tag.TAG_STRING)) {
                        int comp = ca.getString("id").compareTo(cb.getString("id"));
                        if (comp != 0) return comp;
                    }

                    return ca.getShort("lvl") - cb.getShort("lvl");
                }
                return 0;
            });

            enchants = new ListTag();
            enchants.addAll(tags);
            tag.put("Enchantments", enchants);
        }

        return tag;
    }

    public boolean matchesSellingItem(@NotNull ItemStack b) {
        ItemStack a = getSellingItem();
        if (a.isEmpty() || b.isEmpty())
            return false;

        if (!ItemStack.isSameItem(a, b))
            return false;

        CompoundTag an = a.getTag();
        CompoundTag bn = b.getTag();

        if (an == null || bn == null) {
            return an == bn;
        }

        an = cleanTags(an);
        bn = cleanTags(bn);

        return an.equals(bn);
    }

    protected void condenseItems() {
        NonNullList<ItemStack> newItems = NonNullList.withSize(items.size(), ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            newItems.set(i, items.get(i));
        }
        items.clear();

        for (ItemStack stack : newItems) {
            ItemUtil.moveItemStackTo(stack, this, false);
        }
    }

    public void tryTransaction(Player player, InteractionHand hand) {
        switch (mode) {
            case SELL -> trySellTo(player, hand);
            case BUY -> tryBuyFrom(player, hand);
        }
    }

    private void trySellTo(Player player, InteractionHand hand) {
        // condense stock
        // (try to) charge cost
        // dispense stock
        ItemStack selling = getSellingItem();
        if (selling.isEmpty())
            return;

        condenseItems();

        if (isCreativeVendor()) {
            if (price.deduct(player, hand)) {
                ItemStack output = selling.copy();
                ItemUtil.givePlayerItem(player, output);

                level.playSound(null, getBlockPos(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 0.5f, 1.0f);
            } else {
                // insufficient funds
                player.displayClientMessage(Components.translatable("gui.numismatics.vendor.insufficient_funds")
                    .withStyle(ChatFormatting.DARK_RED), true);
                level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
            }
        } else {
            for (ItemStack stack : items) {
                if (matchesSellingItem(stack) && stack.getCount() >= selling.getCount()) {
                    if (price.deduct(player, hand)) {
                        ItemStack output = stack.split(selling.getCount());
                        ItemUtil.givePlayerItem(player, output);

                        level.playSound(null, getBlockPos(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 0.5f, 1.0f);
                    } else {
                        // insufficient funds
                        player.displayClientMessage(Components.translatable("gui.numismatics.vendor.insufficient_funds")
                            .withStyle(ChatFormatting.DARK_RED), true);
                        level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
                    }
                    return;
                }
            }

            // out of stock
            String ownerName = UsernameUtils.INSTANCE.getName(owner, null);
            if (ownerName != null) {
                player.displayClientMessage(Components.translatable("gui.numismatics.vendor.out_of_stock.named", ownerName)
                    .withStyle(ChatFormatting.DARK_RED), true);
                level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
            } else {
                player.displayClientMessage(Components.translatable("gui.numismatics.vendor.out_of_stock")
                    .withStyle(ChatFormatting.DARK_RED), true);
                level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
            }
        }
    }

    private void tryBuyFrom(Player player, InteractionHand hand) {
        ItemStack buying = getSellingItem();
        if (buying.isEmpty())
            return;

        ItemStack handStack = player.getItemInHand(hand);

        if (handStack.isEmpty()) {
            player.displayClientMessage(Components.translatable("gui.numismatics.vendor.no_item_in_hand")
                .withStyle(ChatFormatting.DARK_RED), true);
            level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
            return;
        }

        // check if the held item matches our filter
        if (!matchesSellingItem(handStack)) {
            player.displayClientMessage(Components.translatable("gui.numismatics.vendor.incorrect_item")
                .withStyle(ChatFormatting.DARK_RED), true);
            level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
            return;
        }

        if (handStack.getCount() < buying.getCount()) {
            player.displayClientMessage(Components.translatable("gui.numismatics.vendor.too_few_items")
                .withStyle(ChatFormatting.DARK_RED), true);
            level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
            return;
        }

        // check if the vendor has space
        boolean hasSpace = false;
        int targetStackIdx = 0;
        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                hasSpace = true;
                break;
            }
            if (matchesSellingItem(stack) && stack.getCount() < stack.getMaxStackSize()) {
                hasSpace = true;
                break;
            }
            targetStackIdx++;
        }
        if (!hasSpace) {
            String ownerName = UsernameUtils.INSTANCE.getName(owner, null);
            if (ownerName != null) {
                player.displayClientMessage(Components.translatable("gui.numismatics.vendor.full.named", ownerName)
                    .withStyle(ChatFormatting.DARK_RED), true);
                level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
            } else {
                player.displayClientMessage(Components.translatable("gui.numismatics.vendor.full")
                    .withStyle(ChatFormatting.DARK_RED), true);
                level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
            }
            return;
        }

        // check if the vendor has enough money
        if (isCreativeVendor() || price.canPayOut()) {
            handStack.shrink(buying.getCount());
            player.setItemInHand(hand, handStack);
            items.set(targetStackIdx, buying.copy());

            if (!isCreativeVendor())
                price.deductFromSelf(false);

            price.pay(player);

            level.playSound(null, getBlockPos(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 0.5f, 1.0f);

            return;
        } else if (getCardId() != null) {
            BankAccount account = Numismatics.BANK.getAccount(getCardId());
            if (account != null && account.deduct(price.getTotalPrice())) {
                handStack.shrink(buying.getCount());
                player.setItemInHand(hand, handStack);
                items.set(targetStackIdx, buying.copy());

                price.pay(player);

                level.playSound(null, getBlockPos(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.BLOCKS, 0.5f, 1.0f);

                return;
            }
        }

        // insufficient funds (return early on success)
        String ownerName = UsernameUtils.INSTANCE.getName(owner, null);
        if (ownerName != null) {
            player.displayClientMessage(Components.translatable("gui.numismatics.vendor.insufficient_funds.named", ownerName)
                .withStyle(ChatFormatting.DARK_RED), true);
        } else {
            player.displayClientMessage(Components.translatable("gui.numismatics.vendor.insufficient_funds")
                .withStyle(ChatFormatting.DARK_RED), true);
        }
        level.playSound(null, getBlockPos(), AllSoundEvents.DENY.getMainEvent(), SoundSource.BLOCKS, 0.5f, 1.0f);
    }

    @Override
    public void openTrustListMenu(ServerPlayer player) {
        TrustListMenu.openMenu(this, player, isCreativeVendor()
            ? NumismaticsBlocks.CREATIVE_VENDOR.asStack()
            : NumismaticsBlocks.VENDOR.asStack());
    }

    @Environment(EnvType.CLIENT)
    public void openTrustList() {
        if (level == null || !level.isClientSide)
            return;
        NumismaticsPackets.PACKETS.send(new OpenTrustListPacket<>(this));
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (level != null && !level.isClientSide)
            setChanged();
    }

    public enum Mode {
        SELL,
        BUY
        ;

        private static final List<Component> components = ImmutableList.copyOf(
            Arrays.stream(values())
                .map(Mode::getTranslationKey)
                .map(Components::translatable)
                .iterator()
        );

        public static List<Component> getComponents() {
            return components;
        }

        public String getTranslationKey() {
            return "gui.numismatics.vendor.mode." + name().toLowerCase(Locale.ROOT);
        }

        public Mode getOpposite() {
            return switch (this) {
                case SELL -> BUY;
                case BUY -> SELL;
            };
        }
    }
}