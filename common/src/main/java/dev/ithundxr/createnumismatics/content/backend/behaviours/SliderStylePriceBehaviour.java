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
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.util.ItemUtil;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SliderStylePriceBehaviour extends BlockEntityBehaviour {

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

    public boolean deduct(@NotNull Player player, @NotNull InteractionHand hand, boolean addToSource) {
        int totalPrice = getTotalPrice();

        ItemStack handStack = player.getItemInHand(hand);
        if (NumismaticsTags.AllItemTags.CARDS.matches(handStack)) {
            if (CardItem.isBound(handStack)) {
                UUID id = CardItem.get(handStack);
                BankAccount account = Numismatics.BANK.getAccount(id);
                if (account != null && account.isAuthorized(player)) {
                    if (account.deduct(totalPrice)) {
                        //activate(state, level, pos);
                        if (addToSource) {
                            for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
                                addCoin.accept(entry.getKey(), entry.getValue());
                            }
                        }
                        return true;
                    }
                }
            }
        } else if (CoinItem.extract(player, hand, prices, false)) {
            //activate(state, level, pos);
            if (addToSource) {
                for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
                    addCoin.accept(entry.getKey(), entry.getValue());
                }
            }
            return true;
        }

        return false;
    }

    public boolean canPayOut() {
        return deductFromSelf(true);
    }

    public boolean deductFromSelf(boolean simulate) {
        if (!simulate && !canPayOut())
            return false;

        for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
            Coin coin = entry.getKey();
            int price = entry.getValue();
            int count = getCount.apply(coin);
            if (count < price)
                return false;
            if (!simulate)
                addCoin.accept(coin, -price);
        }
        return true;
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
