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

import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
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

import java.util.UUID;

public interface IDeductable {
    boolean deduct(Coin coin, int amount);
    boolean deduct(int spurs);

    @Nullable
    static IDeductable get(ItemStack stack, @Nullable Player player) {
        if (NumismaticsTags.AllItemTags.CARDS.matches(stack)) {
            if (player == null)
                return null;

            if (player instanceof DeployerFakePlayer)
                return null;

            if (CardItem.isBound(stack)) {
                UUID id = CardItem.get(stack);
                BankAccount account = Numismatics.BANK.getAccount(id);

                if (account != null && account.isAuthorized(player)) {
                    return account;
                }
            }
        } else if (NumismaticsTags.AllItemTags.AUTHORIZED_CARDS.matches(stack)) {
            AuthorizationPair authorizedPair = AuthorizedCardItem.get(stack);

            if (authorizedPair == null)
                return null;

            Authorization authorization;
            if (player == null) {
                authorization = new Authorization.Anonymous(authorizedPair.authorizationID());
            } else if (player instanceof DeployerFakePlayer) {
                authorization = new Authorization.Deployer(player.getUUID(), authorizedPair.authorizationID());
            } else {
                authorization = new Authorization.Player(player, authorizedPair.authorizationID());
            }

            BankAccount account = Numismatics.BANK.getAccount(authorizedPair.accountID());

            if (account == null)
                return null;

            SubAccount subAccount = account.getSubAccount(authorization);

            if (subAccount != null) {
                return subAccount.getDeductor(authorization);
            }
        }

        return null;
    }
}
