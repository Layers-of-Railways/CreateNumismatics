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

package dev.ithundxr.createnumismatics.content.backend.behaviours;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.IDeductable;
import dev.ithundxr.createnumismatics.content.backend.ReasonHolder;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.util.ItemUtil;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SliderStylePriceBehaviour extends BlockEntityBehaviour {

    private boolean clientReadEnabled = true;

    public static final BehaviourType<SliderStylePriceBehaviour> TYPE = new BehaviourType<>("slider-style price");

    protected final EnumMap<Coin, Integer> prices = new EnumMap<>(Coin.class);

    protected final BiConsumer<Coin, Integer> addCoin;
    protected final Function<Coin, Integer> getCount;

    public SliderStylePriceBehaviour(SmartBlockEntity be, BiConsumer<Coin, Integer> addCoin,
                                     Function<Coin, Integer> getCount) {
        super(be);
        this.addCoin = addCoin;
        this.getCount = getCount;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    private int totalPrice = 0;

    private void calculateTotalPrice() {
        totalPrice = 0;
        for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
            totalPrice += entry.getKey().toSpurs(entry.getValue());
        }
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public int getPrice(Coin coin) {
        return prices.getOrDefault(coin, 0);
    }

    public void setPrice(Coin coin, int price) {
        this.prices.put(coin, price);
        calculateTotalPrice();
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        CompoundTag priceTag = new CompoundTag();
        for (Coin coin : Coin.values()) {
            priceTag.putInt(coin.getName(), getPrice(coin));
        }
        tag.put("Prices", priceTag);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        if (clientPacket && !clientReadEnabled)
            return;

        super.read(tag, clientPacket);
        this.prices.clear();
        if (tag.contains("Prices", Tag.TAG_COMPOUND)) {
            CompoundTag priceTag = tag.getCompound("Prices");
            for (Coin coin : Coin.values()) {
                if (priceTag.contains(coin.getName(), Tag.TAG_INT)) {
                    int count = priceTag.getInt(coin.getName());
                    if (count > 0)
                        setPrice(coin, count);
                }
            }
        }
        calculateTotalPrice();
    }

    public void enableClientRead() {
        this.clientReadEnabled = true;
    }

    public void disableClientRead() {
        this.clientReadEnabled = false;
    }

    public int deduct(@NotNull Player player, @NotNull InteractionHand hand, boolean addToSource, ReasonHolder reasonHolder, int maximumCount) {
        int count = 0;

        while (count < maximumCount && deduct(player, hand, addToSource, reasonHolder)) {
            count++;
        }

        return count;
    }

    public boolean deduct(@NotNull Player player, @NotNull InteractionHand hand, boolean addToSource, ReasonHolder reasonHolder) {
        int totalPrice = getTotalPrice();

        ItemStack handStack = player.getItemInHand(hand);
        IDeductable deductable = IDeductable.get(handStack, player, reasonHolder);
        if (deductable != null) {
            if (deductable.deduct(totalPrice, reasonHolder)) {
                //activate(state, level, pos);
                if (addToSource) {
                    addCoinsToSelf();
                }
                return true;
            }
        } else if (CoinItem.extract(player, hand, prices, false)) {
            //activate(state, level, pos);
            if (addToSource) {
                addCoinsToSelf();
            }
            return true;
        }

        return false;
    }

    public void addCoinsToSelf() {
        for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
            addCoin.accept(entry.getKey(), entry.getValue());
        }
    }

    public boolean canPayOut(@Nullable IDeductable deductable) {
        return getMaxAvailablePayOut(1, deductable) > 0;
    }

    /**
     * Get the maximum number of times the price can be paid out
     * @param maxRepetitions the maximum number of times the caller is interested in
     * @param deductable source of non-coin funds
     * @return the actual number of times the price can be paid out
     */
    protected int getMaxAvailablePayOut(int maxRepetitions, @Nullable IDeductable deductable) {
        if (deductable == null)
            deductable = IDeductable.Empty.INSTANCE;

        Map<Coin, Integer> alreadyTakenCoins = new EnumMap<>(Coin.class);
        for (Coin coin : Coin.values())
            alreadyTakenCoins.put(coin, 0);
        int remainingWithdrawal = deductable.getMaxWithdrawal();

        int actualRepetitions = 0;
        for (; actualRepetitions <= maxRepetitions; actualRepetitions++) {
            for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
                Coin coin = entry.getKey();
                int price = entry.getValue();
                int count = getCount.apply(coin) - alreadyTakenCoins.get(coin);
                if (count >= price) {
                    alreadyTakenCoins.put(coin, alreadyTakenCoins.get(coin) + price);
                } else {
                    int remaining = price - count;
                    if (remainingWithdrawal < remaining) {
                        return actualRepetitions;
                    }
                    remainingWithdrawal -= remaining;
                    alreadyTakenCoins.put(coin, alreadyTakenCoins.get(coin) + count);
                }
            }
        }

        return maxRepetitions;
    }

    public int deductFromSelf(int maxRepetitions, @Nullable IDeductable deductable, boolean simulate, @NotNull ReasonHolder reasonHolder) {
        if (deductable == null)
            deductable = IDeductable.Empty.INSTANCE;

        int actualRepetitions = getMaxAvailablePayOut(maxRepetitions, deductable);

        if (simulate || actualRepetitions == 0)
            return actualRepetitions;

        for (int i = 0; i < actualRepetitions; i++) {
            for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
                Coin coin = entry.getKey();
                int price = entry.getValue();
                int count = getCount.apply(coin);
                if (count < price) {
                    if (!deductable.deduct(coin, price - count, reasonHolder)) {
                        Numismatics.crashDev("Failed to deduct from self: " + coin + " " + (price - count));
                    }
                }
                addCoin.accept(coin, -price);
            }
        }

        return actualRepetitions;
    }

    /**
     * Warning: this creates coins, make sure to call {@link #deductFromSelf} before calling this method.
     */
    public void pay(Player player) {
        for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
            Coin coin = entry.getKey();
            int toPay = entry.getValue();
            while (toPay > 0) {
                int count = Math.min(toPay, 64);
                toPay -= count;

                ItemStack stack = coin.asStack(count);
                ItemUtil.givePlayerItem(player, stack);
            }
        }
    }

    public List<MutableComponent> getCondensedPriceBreakdown() {
        List<MutableComponent> components = new ArrayList<>();

        int[] columnWidths = new int[]{0, 0, 0};
        {
            int columnIdx = 0;
            for (int i = 0; i < Coin.values().length; i++) {
                Coin coin = Coin.values()[i];
                int count = prices.getOrDefault(coin, 0);
                if (count > 0) {
                    columnWidths[columnIdx] = Math.max(columnWidths[columnIdx], String.valueOf(count).length());

                    columnIdx = (columnIdx + 1) % 3;
                }
            }
        }


        MutableComponent current = null;
        int countOnCurrent = 0;
        for (int i = 0; i < Coin.values().length; i++) {
            Coin coin = Coin.values()[i];
            int count = prices.getOrDefault(coin, 0);
            if (count > 0) {
                if (current == null) {
                    current = Components.empty();
                }

                if (countOnCurrent++ >= 3) {
                    components.add(current);
                    //components.add(Components.empty());
                    current = Components.empty();
                    countOnCurrent = 1;
                }

                if (countOnCurrent > 1)
                    current.append("  |  ");

                // \uF017 is a 6-wide space (same advance as applied to numbers) defined by Numismatics
                current.append(TextUtils.leftPad(String.valueOf(count), '\uF017', columnWidths[countOnCurrent - 1]) + " " + coin.fontChar);
            }
        }
        if (current != null)
            components.add(current);

        return components;
    }

    public List<MutableComponent> getExtendedPriceBreakdown() {
        List<MutableComponent> components = new ArrayList<>();
        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int count = prices.getOrDefault(coin, 0);
            if (count > 0) {
                components.add(Components.literal(count + " ")
                    .append(Components.translatable(coin.getTranslationKey()))
                    .append(" " + coin.fontChar)
                );
            }
        }
        return components;
    }
}
