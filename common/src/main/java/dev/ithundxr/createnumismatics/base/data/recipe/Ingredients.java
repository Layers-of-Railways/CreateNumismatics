package dev.ithundxr.createnumismatics.base.data.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class Ingredients {
	public static TagKey<Item> string() {
		return conventionalTag("strings");
	}

	public static ItemLike precisionMechanism() {
		return AllItems.PRECISION_MECHANISM.get();
	}

	public static TagKey<Item> ironNugget() {
		return CommonMetal.IRON.nuggets;
	}

	public static TagKey<Item> ironIngot() {
		return CommonMetal.IRON.ingots;
	}

	public static TagKey<Item> zincNugget() {
		return CommonMetal.ZINC.nuggets;
	}

	public static ItemLike girder() {
		return AllBlocks.METAL_GIRDER.get();
	}

	public static ItemLike metalBracket() {
		return AllBlocks.METAL_BRACKET.get();
	}

	public static TagKey<Item> ironSheet() {
		return CommonMetal.IRON.plates;
	}

	public static TagKey<Item> dye(@NotNull DyeColor color) {
		return conventionalTag("dyes/" + color.getSerializedName());
	}

	public static TagKey<Item> fence() {
		return TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace("fences"));
	}

	public static ItemLike campfire() {
		return Blocks.CAMPFIRE;
	}

	public static ItemLike redstone() {
		return Items.REDSTONE;
	}

	public static ItemLike lever() {
		return Items.LEVER;
	}

	public static ItemLike cogwheel() {
		return AllBlocks.COGWHEEL.get();
	}

	public static ItemLike railwayCasing() {
		return AllBlocks.RAILWAY_CASING.get();
	}

	public static ItemLike brassCasing() {
		return AllBlocks.BRASS_CASING.get();
	}

	public static ItemLike andesiteCasing() {
		return AllBlocks.ANDESITE_CASING.get();
	}

	public static ItemLike propeller() {
		return AllItems.PROPELLER.get();
	}

	public static ItemLike electronTube() {
		return AllItems.ELECTRON_TUBE.get();
	}

	public static TagKey<Item> copperIngot() {
		return CommonMetal.COPPER.ingots;
	}

	public static TagKey<Item> brassNugget() {
		return CommonMetal.BRASS.nuggets;
	}

	public static ItemLike phantomMembrane() {
		return Items.PHANTOM_MEMBRANE;
	}

	public static ItemLike eyeOfEnder() {
		return Items.ENDER_EYE;
	}

	public static ItemLike industrialIron() {
		return AllBlocks.INDUSTRIAL_IRON_BLOCK.get();
	}

	public static ItemLike sturdySheet() {
		return AllItems.STURDY_SHEET.get();
	}

	public static ItemLike cogCoin() {
		return NumismaticsItems.getCoin(Coin.COG);
	}

	public static ItemLike paper() {
		return Items.PAPER;
	}

	public static ItemLike framedGlass() {
		return AllPaletteBlocks.FRAMED_GLASS;
	}

	public static ItemLike vendor() {
		return NumismaticsBlocks.VENDOR;
	}

	public static ItemLike placard() {
		return AllBlocks.PLACARD;
	}

	public static TagKey<Item> goldSheet() {
		return CommonMetal.GOLD.plates;
	}

	private static TagKey<Item> conventionalTag(String name) {
		return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
	}
}