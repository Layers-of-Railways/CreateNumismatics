package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AndesiteDepositorBlockEntity extends AbstractDepositorBlockEntity implements MenuProvider, WorldlyContainer {

    private ScrollOptionBehaviour<Coin> coinOption;

    public AndesiteDepositorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        coinOption = new ProtectedScrollOptionBehaviour<>(Coin.class, Components.translatable("create.numismatics.andesite_depositor.price"), this,
            new DepositorValueBoxTransform(), this::isTrusted);
        behaviours.add(coinOption);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Coin coin = coinOption.get();
        Lang.builder()
            .add(Components.translatable("block.numismatics.andesite_depositor.tooltip.price",
                    1, Components.translatable(coin.getTranslationKey()), coin.value
                ).withStyle(coin.rarity.color)
            )
            .forGoggles(tooltip);
        return true;
    }

    public Coin getCoin() {
        return coinOption.get();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Components.translatable("block.numismatics.andesite_depositor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        if (!isTrusted(player))
            return null;
        return new AndesiteDepositorMenu(NumismaticsMenuTypes.ANDESITE_DEPOSITOR.get(), i, inventory, this);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);

        if (!inputStack.isEmpty())
            tag.put("InputStack", inputStack.save(new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        if (tag.contains("InputStack", Tag.TAG_COMPOUND)) {
            inputStack = ItemStack.of(tag.getCompound("InputStack"));
        } else {
            inputStack = ItemStack.EMPTY;
        }
    }

    @Override
    public void lazyTick() {
        if (level != null && !level.isClientSide) {
            if (!inputStack.isEmpty() && inputStack.getItem() instanceof CoinItem coinItem) {
                inventory.add(coinItem.coin, inputStack.getCount());
                setChanged();
                if (coinItem.coin == getCoin())
                    activate();
            }
            inputStack = ItemStack.EMPTY;
        }
        super.lazyTick();
    }

    protected boolean isContainerActive() {
        return !getBlockState().getValue(AndesiteDepositorBlock.LOCKED) && !getBlockState().getValue(AndesiteDepositorBlock.POWERED);
    }

    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] NO_SLOTS = new int[0];

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        if (side == Direction.UP && isContainerActive())
            return SLOTS_FOR_UP;
        return NO_SLOTS;
    }

    // this abstraction is needed to allow mods to simulate adding/removing items before coins are put into CoinBag or bank account
    @NotNull
    private ItemStack inputStack = ItemStack.EMPTY;

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStack, @Nullable Direction direction) {
        return isContainerActive() && direction == Direction.UP
            && itemStack.getItem() instanceof CoinItem coinItem && coinItem.coin == getCoin();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return isContainerActive() ? 1 : 0;
    }

    @Override
    public boolean isEmpty() {
        return !isContainerActive() || inputStack.isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return inputStack;
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (inputStack.isEmpty())
            return ItemStack.EMPTY;
        return inputStack.split(amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack ret = inputStack;
        inputStack = ItemStack.EMPTY;
        return ret;
    }
    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        inputStack = stack;
        setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        inputStack = ItemStack.EMPTY;
    }
}
