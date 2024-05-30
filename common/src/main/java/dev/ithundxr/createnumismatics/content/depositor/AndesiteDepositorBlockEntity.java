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

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListMenu;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

    private @NotNull Coin coin = Coin.SPUR;

    public AndesiteDepositorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Coin coin = getCoin();
        Lang.builder()
            .add(Components.translatable("block.numismatics.andesite_depositor.tooltip.price",
                    1,
                Components.translatable(coin.getTranslationKey())
                    .append(Components.literal(" " + coin.fontChar).withStyle(ChatFormatting.WHITE)),
                coin.value
                ).withStyle(coin.rarity.color)
            )
            .forGoggles(tooltip);
        return true;
    }

    public void setCoin(@NotNull Coin coin) {
        this.coin = coin;
    }

    public @NotNull Coin getCoin() {
        return coin;
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
        tag.putInt("Coin", coin.ordinal());
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        if (tag.contains("InputStack", Tag.TAG_COMPOUND)) {
            inputStack = ItemStack.of(tag.getCompound("InputStack"));
        } else {
            inputStack = ItemStack.EMPTY;
        }

        int coinIdx = 0;
        if (tag.contains("ScrollValue", Tag.TAG_INT))
            coinIdx = tag.getInt("ScrollValue");
        else if (tag.contains("Coin", Tag.TAG_INT))
            coinIdx = tag.getInt("Coin");
        coin = Coin.values()[coinIdx];
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

    @Override
    public void openTrustListMenu(ServerPlayer player) {
        TrustListMenu.openMenu(this, player, NumismaticsBlocks.ANDESITE_DEPOSITOR.asStack());
    }
}
