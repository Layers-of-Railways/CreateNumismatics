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

package dev.ithundxr.createnumismatics.content.backend;

import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.Authorization;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.SubAccount;
import dev.ithundxr.createnumismatics.content.bank.AuthorizedCardItem;
import dev.ithundxr.createnumismatics.content.bank.AuthorizedCardItem.AuthorizationPair;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface IDeductable {
    boolean deduct(Coin coin, int amount, ReasonHolder reasonHolder);
    boolean deduct(int spurs, ReasonHolder reasonHolder);
    int getMaxWithdrawal();

    @Nullable
    static IDeductable get(ItemStack stack, @Nullable Player player, ReasonHolder reasonHolder) {
        return IDeductable.getInternal(stack, player == null ? null : Either.left(player), reasonHolder);
    }

    @Nullable
    static IDeductable getAutomated(ItemStack stack, @Nullable UUID owningPlayer, ReasonHolder reasonHolder) {
        return IDeductable.getInternal(stack, owningPlayer == null ? null : Either.right(owningPlayer), reasonHolder);
    }

    @Nullable
    private static IDeductable getInternal(ItemStack stack, @Nullable Either<Player, UUID> player, ReasonHolder reasonHolder) {
        if (NumismaticsTags.AllItemTags.CARDS.matches(stack)) {
            if (player == null)
                return null;

            Optional<Player> left = player.left();
            if (left.isEmpty())
                return null;

            Player player$ = left.get();
            if (player$ instanceof DeployerFakePlayer)
                return null;

            if (CardItem.isBound(stack)) {
                UUID id = CardItem.get(stack);
                BankAccount account = Numismatics.BANK.getAccount(id);

                if (account != null && account.isAuthorized(player$)) {
                    return account;
                } else if (account == null) {
                    reasonHolder.setMessage(Components.translatable("error.numismatics.card.account_not_found"));
                } else {
                    reasonHolder.setMessage(Components.translatable("error.numismatics.card.not_authorized"));
                }
            } else {
                reasonHolder.setMessage(Components.translatable("error.numismatics.card.not_bound"));
            }
        } else if (NumismaticsTags.AllItemTags.AUTHORIZED_CARDS.matches(stack)) {
            AuthorizationPair authorizedPair = AuthorizedCardItem.get(stack);

            if (authorizedPair == null) {
                reasonHolder.setMessage(Components.translatable("error.numismatics.card.not_bound"));
                return null;
            }

            Authorization authorization;
            if (player == null) {
                authorization = new Authorization.Anonymous(authorizedPair.authorizationID());
            } else {
                authorization = player.map(
                    p -> p instanceof DeployerFakePlayer
                        ? new Authorization.Automation(p.getUUID(), authorizedPair.authorizationID())
                        : new Authorization.Player(p, authorizedPair.authorizationID()),
                    uuid -> new Authorization.Automation(uuid, authorizedPair.authorizationID())
                );
            }

            BankAccount account = Numismatics.BANK.getAccount(authorizedPair.accountID());

            if (account == null) {
                reasonHolder.setMessage(Components.translatable("error.numismatics.card.account_not_found"));
                return null;
            }

            SubAccount subAccount = account.getSubAccount(authorization, reasonHolder);

            if (subAccount != null) {
                IDeductable deductor = subAccount.getDeductor(authorization);
                if (deductor == null) {
                    reasonHolder.setMessage(Components.translatable("error.numismatics.card.not_authorized"));
                }
                return deductor;
            } else if (!reasonHolder.hasMessage()) {
                reasonHolder.setMessage(Components.translatable("error.numismatics.authorized_card.account_not_found"));
            }
        }

        return null;
    }
}
