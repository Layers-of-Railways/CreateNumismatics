/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
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

package dev.ithundxr.createnumismatics.base.data;

import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.RegistrateItemTagsProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags.AllBlockTags;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags.AllItemTags;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on {@link TagGen}
 */
public class NumismaticsTagGen {
    private static final Map<TagKey<Block>, List<ResourceLocation>> OPTIONAL_TAGS = new HashMap<>();

    @SafeVarargs
    public static void addOptionalTag(ResourceLocation id, TagKey<Block>... tags) {
        for (TagKey<Block> tag : tags) {
            OPTIONAL_TAGS.computeIfAbsent(tag, (e) -> new ArrayList<>()).add(id);
        }
    }

    public static void generateBlockTags(RegistrateTagsProvider<Block> tags) {
        addTagToAllInRegistry(tags, BuiltInRegistries.BLOCK, NumismaticsTags.AllBlockTags.NUMISMATICS_BLOCKS.tag);

        for (TagKey<Block> tag : OPTIONAL_TAGS.keySet()) {
            TagsProvider.TagAppender<Block> appender = tagAppender(tags, tag);
            for (ResourceLocation loc : OPTIONAL_TAGS.get(tag))
                appender.addOptional(loc);
        }
    }

    public static void generateItemTags(RegistrateItemTagsProvider tags) {
        addTagToAllInRegistry(tags, BuiltInRegistries.ITEM, NumismaticsTags.AllItemTags.NUMISMATICS_ITEMS.tag);

        for (AllItemTags tag : AllItemTags.values()) {
            if (tag.alwaysDatagen)
                tagAppender(tags, tag);
        }
    }

    public static TagsProvider.TagAppender<Item> tagAppender(RegistrateItemTagsProvider prov, AllItemTags tag) {
        return tagAppender(prov, tag.tag);
    }

    public static TagsProvider.TagAppender<Block> tagAppender(RegistrateTagsProvider<Block> prov, AllBlockTags tag) {
        return tagAppender(prov, tag.tag);
    }

    public static <T> TagsProvider.TagAppender<T> tagAppender(RegistrateTagsProvider<T> prov, TagKey<T> tag) {
        return prov.addTag(tag);
    }

    @SuppressWarnings("unchecked")
    public static <T> void addTagToAllInRegistry(RegistrateTagsProvider<T> prov, DefaultedRegistry<T> defaultedRegistry, TagKey<T> tagKey) {
        ResourceKey<T>[] array = (ResourceKey<T>[]) defaultedRegistry.registryKeySet()
                .stream()
                .filter(rs -> rs.location().getNamespace().equals(Numismatics.MOD_ID))
                .sorted(Comparator.comparing(rs -> rs.location().getPath()))
                .toArray(ResourceKey[]::new);

        prov.addTag(tagKey)
                .add(array);
    }
}
