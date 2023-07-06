package dev.ithundxr.createnumismatics.base.data.forge;

import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraft.tags.TagKey;

public class NumismaticsTagGenImpl {
	public static <T> TagAppender<T> tagAppender(RegistrateTagsProvider<T> prov, TagKey<T> tag) {
		return prov.tag(tag);
	}
}
