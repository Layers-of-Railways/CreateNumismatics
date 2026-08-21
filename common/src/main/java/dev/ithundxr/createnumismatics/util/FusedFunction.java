/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/** A wrapper for a {@link Function} that will delegate {@link Function#apply(T)} once, then store the result. */
public class FusedFunction<T, R> implements Function<T, R> {
    private @Nullable Function<T, R> function;
    private @Nullable R value;

    public FusedFunction(@NotNull Function<T, R> function) {
        this.function = Objects.requireNonNull(function);
        this.value = null;
    }

    public FusedFunction(@SuppressWarnings("NullableProblems") R value) {
        this.function = null;
        this.value = value;
    }

    @Override
    public R apply(T t) {
        if (function != null) {
            value = function.apply(t);
            function = null;
        }

        return value;
    }
}
