/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.compat.computercraft.forge;

import com.google.common.collect.ImmutableMap;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dev.ithundxr.createnumismatics.compat.computercraft.ComputerCraftProxy;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.ComputerBehaviour;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.BankTerminalPeripheral;
import dev.ithundxr.createnumismatics.content.bank.AuthorizedCardItem;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class ComputerCraftProxyImpl {
    public static void registerWithDependency() {
        /* Comment if computercraft.implementation is not in the source set */
        ComputerCraftProxy.computerFactory = ComputerBehaviour::new;

        ForgeComputerCraftAPI.registerPeripheralProvider((level, pos, direction) -> {
            BlockState state = level.getBlockState(pos);

            if (NumismaticsBlocks.BANK_TERMINAL.has(state)) {
                return LazyOptional.of(() -> BankTerminalPeripheral.INSTANCE);
            }

            return LazyOptional.empty();
        });

        VanillaDetailRegistries.ITEM_STACK.addProvider((detailMap, stack) -> {
            Map<Object, @Nullable Object> cardDetails = null;
            if (NumismaticsTags.AllItemTags.CARDS.matches(stack)) {
                UUID accountID = CardItem.get(stack);
                if (accountID != null) {
                    cardDetails = Map.of(
                        "AccountID", accountID.toString()
                    );
                }
            } else if (NumismaticsTags.AllItemTags.AUTHORIZED_CARDS.matches(stack)) {
                AuthorizedCardItem.AuthorizationPair authorizationPair = AuthorizedCardItem.get(stack);
                if (authorizationPair != null) {
                    UUID accountID = authorizationPair.accountID();
                    UUID authorizationID = authorizationPair.authorizationID();
                    cardDetails = Map.of(
                        "AccountID", accountID.toString(),
                        "AuthorizationID", authorizationID.toString()
                    );
                }
            } else if (NumismaticsTags.AllItemTags.ID_CARDS.matches(stack)) {
                UUID id = IDCardItem.get(stack);
                if (id != null) {
                    cardDetails = Map.of(
                        "ID", id.toString()
                    );
                }
            }

            if (cardDetails != null)
                detailMap.put("numismatics", ImmutableMap.of("card", cardDetails));
        });
    }
    public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
        if (ComputerCraftProxy.computerFactory == null)
            return ComputerCraftProxy.fallbackFactory.apply(sbe);
        return ComputerCraftProxy.computerFactory.apply(sbe);
    }
}
