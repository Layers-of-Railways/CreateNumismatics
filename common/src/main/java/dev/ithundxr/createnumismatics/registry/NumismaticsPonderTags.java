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

package dev.ithundxr.createnumismatics.registry;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class NumismaticsPonderTags {
    public static final ResourceLocation SHOPS = loc("shops");

    @SuppressWarnings("SameParameterValue")
    private static ResourceLocation loc(String id) {
        return Numismatics.asResource(id);
    }

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        helper.registerTag(SHOPS)
            .addToIndex()
            .item(NumismaticsBlocks.VENDOR.get(), true, false)
            .title("Shops")
            .description("Components which perform monetary transactions")
            .register();

        HELPER.addToTag(SHOPS)
            .add(NumismaticsBlocks.ANDESITE_DEPOSITOR)
            .add(NumismaticsBlocks.BRASS_DEPOSITOR)
            .add(NumismaticsBlocks.VENDOR)
            .add(NumismaticsBlocks.CREATIVE_VENDOR)
            .add(NumismaticsBlocks.SALEPOINT);
    }
}
