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

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderTag;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;

public class NumismaticsPonderTags {
    private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

    public static final PonderTag SHOPS = create("shops", NumismaticsBlocks.VENDOR, Registries.BLOCK)
        .defaultLang("Shops", "Components which perform monetary transactions")
        .addToIndex();

    @SuppressWarnings("SameParameterValue")
    private static <R extends ItemLike, T extends R> PonderTag create(String id, ItemProviderEntry<T> entry, ResourceKey<? extends Registry<R>> registry) {
        PonderTag tag = new PonderTag(Numismatics.asResource(id));
        ItemLike entry$ = entry.getUnchecked();
        if (entry$ != null) {
            tag.item(entry$);
        } else {
            REGISTRATE.addRegisterCallback(entry.getId().getPath(), registry, tag::item);
        }
        return tag;
    }

    public static void register() {
        if (!REGISTRATE.isRegistered(Registries.BLOCK)) {
            REGISTRATE.addRegisterCallback(Registries.BLOCK, NumismaticsPonderTags::$register);
        } else {
            $register();
        }
    }

    private static void $register() {
        PonderRegistry.TAGS.forTag(SHOPS)
            .add(NumismaticsBlocks.ANDESITE_DEPOSITOR)
            .add(NumismaticsBlocks.BRASS_DEPOSITOR)
            .add(NumismaticsBlocks.VENDOR)
            .add(NumismaticsBlocks.CREATIVE_VENDOR)
            .add(NumismaticsBlocks.SALEPOINT);
    }
}
