/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.content.coins;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class LinkedMergingCoinBag extends MergingCoinBag {
    protected abstract int getDelegate();
    protected abstract void setDelegate(int value);

    @Override
    public int getValue() {
        return getDelegate();
    }

    @Override
    protected void setRaw(int value) {
        setDelegate(value);
    }

    public static class FunctionalLinkedMergingCoinBag extends LinkedMergingCoinBag {

        private final Consumer<Integer> setter;
        private final Supplier<Integer> getter;

        public FunctionalLinkedMergingCoinBag(Consumer<Integer> setter, Supplier<Integer> getter) {
            this.setter = setter;
            this.getter = getter;
        }

        @Override
        protected int getDelegate() {
            return getter.get();
        }

        @Override
        protected void setDelegate(int value) {
            setter.accept(value);
        }
    }
}
