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

import dev.ithundxr.createnumismatics.content.backend.sub_authorization.Authorization;

/**
 * This is a marker interface, which should be used on {@link IDeductable} implementations that re-check the authorization before every deduction.
 */
public interface IAuthorizationCheckingDeductable extends IDeductable {

    static IAuthorizationCheckingDeductable of(IDeductable deductable, Authorization authorization, IAuthorizationChecker checker) {
        if (deductable instanceof IAuthorizationCheckingDeductable authorizationCheckingDeductable)
            return authorizationCheckingDeductable;

        return new AuthorizationCheckingDeductableImpl(deductable, authorization, checker);
    }
}
