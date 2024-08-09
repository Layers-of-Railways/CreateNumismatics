/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.base.data.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.multiloader.CommonTags;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class NumismaticsRecipeProvider extends RecipeProvider {

  protected final List<GeneratedRecipe> all = new ArrayList<>();

  public NumismaticsRecipeProvider(PackOutput pOutput) {
    super(pOutput);
  }

  @Override
  public void buildRecipes(@NotNull Consumer<FinishedRecipe> p_200404_1_) {
    all.forEach(c -> c.register(p_200404_1_));
    Numismatics.LOGGER.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
  }

  protected GeneratedRecipe register(GeneratedRecipe recipe) {
    all.add(recipe);
    return recipe;
  }

  @FunctionalInterface
  public interface GeneratedRecipe {
    void register(Consumer<FinishedRecipe> consumer);
  }

  @SuppressWarnings("SameReturnValue")
  public static class Ingredients {
    public static TagKey<Item> string() {
      return CommonTags.STRING.tag;
    }

    public static ItemLike precisionMechanism() {
      return AllItems.PRECISION_MECHANISM.get();
    }

    public static TagKey<Item> ironNugget() {
      return CommonTags.IRON_NUGGETS.tag;
    }

    public static TagKey<Item> ironIngot() {
      return CommonTags.IRON_INGOTS.tag;
    }

    public static TagKey<Item> zincNugget() {
      return CommonTags.ZINC_NUGGETS.tag;
    }

    public static ItemLike girder() {
      return AllBlocks.METAL_GIRDER.get();
    }

    public static ItemLike metalBracket() {
      return AllBlocks.METAL_BRACKET.get();
    }

    public static TagKey<Item> ironSheet() {
      return CommonTags.IRON_PLATES.tag;
    }

    public static TagKey<Item> goldSheet() {
      return CommonTags.GOLD_PLATES.tag;
    }

    public static TagKey<Item> dye(@NotNull DyeColor color) {
      return CommonTags.DYES.get(color).tag;
    }

    public static TagKey<Item> fence() {
      return TagKey.<Item>create(Registries.ITEM, new ResourceLocation("minecraft:fences"));
    }

    public static ItemLike campfire() {
      return Blocks.CAMPFIRE;
    }

    public static ItemLike redstone() {
      return Items.REDSTONE;
    }

    public static ItemLike lever() { return Items.LEVER; }

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
      return CommonTags.COPPER_INGOTS.tag;
    }

    public static TagKey<Item> brassNugget() {
      return CommonTags.BRASS_NUGGETS.tag;
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
      return NumismaticsBlocks.VENDOR.get();
    }

    public static ItemLike placard() {
      return AllBlocks.PLACARD.get();
    }
  }
}
