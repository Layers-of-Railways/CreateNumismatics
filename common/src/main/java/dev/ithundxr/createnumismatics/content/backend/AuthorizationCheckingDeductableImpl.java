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

import com.simibubi.create.foundation.utility.Components;
import dev.ithundxr.createnumismatics.content.backend.sub_authorization.Authorization;
import org.jetbrains.annotations.NotNull;

class AuthorizationCheckingDeductableImpl implements IAuthorizationCheckingDeductable {
    private final @NotNull IDeductable wrapped;
    private final @NotNull Authorization authorization;
    private final @NotNull IAuthorizationChecker authorizationChecker;

    AuthorizationCheckingDeductableImpl(@NotNull IDeductable wrapped, @NotNull Authorization authorization, @NotNull IAuthorizationChecker authorizationChecker) {
        this.wrapped = wrapped;
        this.authorization = authorization;
        this.authorizationChecker = authorizationChecker;
    }

    @Override
    public boolean deduct(Coin coin, int amount, ReasonHolder reasonHolder) {
        if (!authorizationChecker.isAuthorized(authorization)) {
            reasonHolder.setMessage(Components.translatable("error.numismatics.card.not_authorized"));
            return false;
        }

        return wrapped.deduct(coin, amount, reasonHolder);
    }

    @Override
    public boolean deduct(int spurs, ReasonHolder reasonHolder) {
        if (!authorizationChecker.isAuthorized(authorization)) {
            reasonHolder.setMessage(Components.translatable("error.numismatics.card.not_authorized"));
            return false;
        }

        return wrapped.deduct(spurs, reasonHolder);
    }

    @Override
    public int getMaxWithdrawal() {
        if (!authorizationChecker.isAuthorized(authorization))
            return 0;

        return wrapped.getMaxWithdrawal();
    }
}
