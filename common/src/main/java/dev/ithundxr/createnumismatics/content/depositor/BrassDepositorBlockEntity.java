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

package dev.ithundxr.createnumismatics.content.depositor;

import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import dev.ithundxr.createnumismatics.compat.computercraft.ComputerCraftProxy;
import dev.ithundxr.createnumismatics.config.NumismaticsConfig;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceBehaviour;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListMenu;
import dev.ithundxr.createnumismatics.content.coins.MergingCoinBag;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrassDepositorBlockEntity extends AbstractDepositorBlockEntity implements MenuProvider {

    private SliderStylePriceBehaviour price;
    public AbstractComputerBehaviour computerBehaviour;

    public BrassDepositorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        price = new SliderStylePriceBehaviour(this, this::addCoin, this::getCoinCount);
        behaviours.add(computerBehaviour = ComputerCraftProxy.behaviour(this));
        behaviours.add(price);
    }

    public int getCoinCount(Coin coin) {
        return this.inventory.getDiscrete(coin);
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

    public int getTotalPrice() {
        return price.getTotalPrice();
    }

    public int getPrice(Coin coin) {
        return price.getPrice(coin);
    }

    public void setPrice(Coin coin, int price) {
        this.price.setPrice(coin, price);
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
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Couple<Integer> referenceAndSpurs = NumismaticsConfig.common().referenceCoin.get().convert(price.getTotalPrice());
        int cogs = referenceAndSpurs.getFirst();
        int spurs = referenceAndSpurs.getSecond();
        MutableComponent balanceLabel = Components.translatable("block.numismatics.brass_depositor.tooltip.price",
            TextUtils.formatInt(cogs), NumismaticsConfig.common().referenceCoin.get().getName(cogs), spurs);
        Lang.builder()
            .add(balanceLabel.withStyle(Coin.closest(price.getTotalPrice()).rarity.color))
            .forGoggles(tooltip);

        for (MutableComponent component : price.getCondensedPriceBreakdown()) {
            Lang.builder()
                .add(component)
                .forGoggles(tooltip);
        }
        return true;
    }

    @Override
    public void openTrustListMenu(ServerPlayer player) {
        TrustListMenu.openMenu(this, player, NumismaticsBlocks.BRASS_DEPOSITOR.asStack());
    }
}
