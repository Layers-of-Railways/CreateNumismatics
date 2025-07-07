package dev.ithundxr.createnumismatics.base.data;

import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.RegistrateItemTagsProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags.AllBlockTags;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags.AllItemTags;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
        for (TagKey<Block> tag : OPTIONAL_TAGS.keySet()) {
            var appender = tagAppender(tags, tag);
            for (ResourceLocation loc : OPTIONAL_TAGS.get(tag))
                appender.addOptional(loc);
        }
    }

    public static void generateItemTags(RegistrateItemTagsProvider tags) {
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
