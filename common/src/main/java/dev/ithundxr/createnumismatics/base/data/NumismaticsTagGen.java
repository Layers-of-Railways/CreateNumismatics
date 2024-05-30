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

package dev.ithundxr.createnumismatics.base.data;

import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.RegistrateItemTagsProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.multiloader.CommonTags;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags.AllBlockTags;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags.AllItemTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
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
            OPTIONAL_TAGS.computeIfAbsent(tag, (e) -> new ArrayList<ResourceLocation>()).add(id);
        }
    }
    public static void generateBlockTags(RegistrateTagsProvider<Block> tags) {
//		tagAppender(tags, AllBlockTags.TRACKS)
//			.add(AllBlocks.TRACK.get());
        CommonTags.RELOCATION_NOT_SUPPORTED.generateBoth(tags, tag -> {
                tag.addTag(CommonTags.RELOCATION_NOT_SUPPORTED.tag);
        });
        for (TagKey<Block> tag : OPTIONAL_TAGS.keySet()) {
            var appender = tagAppender(tags, tag);
            for (ResourceLocation loc : OPTIONAL_TAGS.get(tag))
                appender.addOptional(loc);
        }
    }

    public static void generateItemTags(RegistrateItemTagsProvider tags) {
        CommonTags.DYES.values().forEach(tag -> tag.generateCommon(tags));
        CommonTags.IRON_NUGGETS.generateCommon(tags);
        CommonTags.ZINC_NUGGETS.generateCommon(tags);
        CommonTags.BRASS_NUGGETS.generateCommon(tags);
        CommonTags.COPPER_INGOTS.generateCommon(tags);
        CommonTags.IRON_INGOTS.generateCommon(tags);
        CommonTags.STRING.generateCommon(tags)
            .generateBoth(tags, tag -> tag.add(Items.STRING.builtInRegistryHolder().key()));
        CommonTags.IRON_PLATES.generateCommon(tags);
//			.generateBoth(tags, tag -> tag.add(AllItems.IRON_SHEET.get()));

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

    @ExpectPlatform
    public static <T> TagsProvider.TagAppender<T> tagAppender(RegistrateTagsProvider<T> prov, TagKey<T> tag) {
        throw new AssertionError();
    }
}
