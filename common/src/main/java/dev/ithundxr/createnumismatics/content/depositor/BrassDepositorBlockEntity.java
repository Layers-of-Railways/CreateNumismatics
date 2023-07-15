package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.MergingCoinBag;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BrassDepositorBlockEntity extends AbstractDepositorBlockEntity implements MenuProvider {

    protected final EnumMap<Coin, Integer> prices = new EnumMap<>(Coin.class);

    public BrassDepositorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public @NotNull Component getDisplayName() {
        return Components.translatable("block.numismatics.brass_depositor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        if (!isTrusted(player))
            return null;
        return new BrassDepositorMenu(NumismaticsMenuTypes.BRASS_DEPOSITOR.get(), i, inventory, this);
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
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        CompoundTag priceTag = new CompoundTag();
        for (Coin coin : Coin.values()) {
            priceTag.putInt(coin.getName(), getPrice(coin));
        }
        tag.put("Prices", priceTag);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
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

    public void addCoins(int totalPrice) {
        MergingCoinBag coinBag = new MergingCoinBag(totalPrice);

        for (int i = Coin.values().length - 1; i >= 0; i--) {
            Coin coin = Coin.values()[i];
            int count = coinBag.get(coin).getFirst();
            if (count > 0) {
                coinBag.subtract(coin, count);
                addCoin(coin, count);
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Couple<Integer> cogsAndSpurs = Coin.COG.convert(getTotalPrice());
        int cogs = cogsAndSpurs.getFirst();
        int spurs = cogsAndSpurs.getSecond();
        MutableComponent balanceLabel = Components.translatable("block.numismatics.brass_depositor.tooltip.price",
            TextUtils.formatInt(cogs), Coin.COG.getName(cogs), spurs);
        Lang.builder()
            .add(balanceLabel.withStyle(Coin.closest(getTotalPrice()).rarity.color))
            .forGoggles(tooltip);
        return true;
    }
}
