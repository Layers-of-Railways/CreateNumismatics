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

package dev.ithundxr.createnumismatics.ponder.utils;

import com.simibubi.create.foundation.ponder.PonderLocalization;
import dev.ithundxr.createnumismatics.Numismatics;

public class NumismaticsSharedText {
    public static void gatherText() {
        // Add entries used across several ponder scenes (Safe for hotswap)
        
        createNumberPonderEntries(1, 10);
    }

    private static void createNumberPonderEntries(int from, int to) {
        for (int i = from; i < to; i++) {
            add("amount" + i + "x", i + "x");
            add("amount_spaced_" + i + "x", i + "x ");
        }
    }

    private static void add(String k, String v) {
        PonderLocalization.registerShared(Numismatics.asResource(k), v);
    }
}
