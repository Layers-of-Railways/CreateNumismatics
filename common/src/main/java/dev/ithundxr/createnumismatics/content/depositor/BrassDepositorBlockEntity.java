package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListMenu;
import dev.ithundxr.createnumismatics.content.coins.MergingCoinBag;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.util.TextUtils;
import dev.ithundxr.createnumismatics.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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

    private ScrollOptionBehaviour<TrustListSham> trustListButton;

    public BrassDepositorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private enum TrustListSham implements INamedIconOptions {
        NONE;

        @Override
        public AllIcons getIcon() {
            return AllIcons.I_VIEW_SCHEDULE;
        }

        @Override
        public String getTranslationKey() {
            return "numismatics.trust_list.configure";
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        trustListButton = new ProtectedScrollOptionBehaviour<>(TrustListSham.class, Components.translatable("create.numismatics.trust_list.configure"), this,
            new DepositorValueBoxTransform(), this::isTrusted) {
            @Override
            public void onShortInteract(Player player, InteractionHand hand, Direction side) {
                if (isTrusted(player) && player instanceof ServerPlayer serverPlayer) {
                    Utils.openScreen(serverPlayer,
                        TrustListMenu.provider(BrassDepositorBlockEntity.this, NumismaticsBlocks.BRASS_DEPOSITOR.asStack()),
                        (buf) -> {
                            buf.writeItem(NumismaticsBlocks.BRASS_DEPOSITOR.asStack());
                            BrassDepositorBlockEntity.this.sendToMenu(buf);
                        });
                } else {
                    super.onShortInteract(player, hand, side);
                }
            }

            @Override
            public boolean acceptsValueSettings() {
                return false;
            }
        };
        behaviours.add(trustListButton);
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
