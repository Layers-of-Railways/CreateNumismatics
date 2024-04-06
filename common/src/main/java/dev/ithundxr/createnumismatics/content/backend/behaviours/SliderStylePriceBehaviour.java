package dev.ithundxr.createnumismatics.content.backend.behaviours;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
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

    public boolean deduct(@NotNull Player player, @NotNull InteractionHand hand) {
        int totalPrice = getTotalPrice();

        ItemStack handStack = player.getItemInHand(hand);
        if (NumismaticsTags.AllItemTags.CARDS.matches(handStack)) {
            if (CardItem.isBound(handStack)) {
                UUID id = CardItem.get(handStack);
                BankAccount account = Numismatics.BANK.getAccount(id);
                if (account != null && account.isAuthorized(player)) {
                    if (account.deduct(totalPrice)) {
                        //activate(state, level, pos);
                        for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
                            addCoin.accept(entry.getKey(), entry.getValue());
                        }
                        return true;
                    }
                }
            }
        } else if (CoinItem.extract(player, hand, prices, false)) {
            //activate(state, level, pos);
            for (Map.Entry<Coin, Integer> entry : prices.entrySet()) {
                addCoin.accept(entry.getKey(), entry.getValue());
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
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
        }
    }
}
