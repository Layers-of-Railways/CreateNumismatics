package dev.ithundxr.createnumismatics.base.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class CommonTags {
	public static class Blocks {
		public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = tag("relocation_not_supported");

		private static TagKey<Block> tag(String name) {
			return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", name));
		}
	}
}
