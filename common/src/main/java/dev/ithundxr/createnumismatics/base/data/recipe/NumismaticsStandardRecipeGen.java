/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.base.data.recipe;

import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class NumismaticsStandardRecipeGen extends NumismaticsRecipeProvider {

    /*GeneratedRecipe TRACK_COUPLER = create(CRBlocks.TRACK_COUPLER)
        .unlockedBy(Ingredients::railwayCasing)
        .viaShaped(b -> b.define('=', Ingredients.ironSheet())
            .define('#', Ingredients.redstone())
            .define('T', Ingredients.railwayCasing())
            .pattern("=")
            .pattern("#")
            .pattern("T"));

    GeneratedRecipe CONDUCTOR_WHISTLE = create(CRBlocks.CONDUCTOR_WHISTLE_FLAG)
        .unlockedByTag(() -> CRTags.AllItemTags.CONDUCTOR_CAPS.tag)
        .viaShapeless(b -> b
            .requires(Ingredients.copperIngot())
            .requires(Ingredients.brassNugget()));*/

    GeneratedRecipe ANDESITE_DEPOSITOR = create(NumismaticsBlocks.ANDESITE_DEPOSITOR)
        .unlockedBy(Ingredients::andesiteCasing)
        .viaShapeless(b -> b
            .requires(Ingredients.andesiteCasing())
            .requires(Ingredients.ironSheet()));

    GeneratedRecipe BRASS_DEPOSITOR = create(NumismaticsBlocks.BRASS_DEPOSITOR)
        .unlockedBy(Ingredients::brassCasing)
        .viaShapeless(b -> b
            .requires(Ingredients.brassCasing())
            .requires(Ingredients.sturdySheet())
            .requires(Ingredients.electronTube()));

    GeneratedRecipe BANK_TERMINAL = create(NumismaticsBlocks.BANK_TERMINAL)
        .unlockedBy(Ingredients::precisionMechanism)
        .viaShapeless(b -> b
            .requires(Ingredients.precisionMechanism())
            .requires(Ingredients.industrialIron())
            .requires(Ingredients.electronTube()));

    GeneratedRecipe BANKING_GUIDE = create(NumismaticsItems.BANKING_GUIDE)
        .unlockedBy(Ingredients::cogCoin)
        .viaShapeless(b -> b
            .requires(Ingredients.cogCoin())
            .requires(Ingredients.sturdySheet())
            .requires(Ingredients.paper()));

    GeneratedRecipe VENDOR = create(NumismaticsBlocks.VENDOR)
        .unlockedBy(Ingredients::brassCasing)
        .viaShaped(b -> b
            .pattern("o")
            .pattern("#")
            .pattern("I")
            .define('o', Ingredients.framedGlass())
            .define('#', Ingredients.brassCasing())
            .define('I', Ingredients.electronTube()));

    DyedRecipeList CARDS = new DyedRecipeList(color -> create(NumismaticsItems.CARDS.get(color))
        .unlockedBy(Ingredients::precisionMechanism)
        .viaShaped(b -> b
            .pattern("@_/")
            .define('@', Ingredients.precisionMechanism())
            .define('_', Ingredients.ironSheet())
            .define('/', Ingredients.dye(color)))
    );

    DyedRecipeList ID_CARDS = new DyedRecipeList(color -> create(NumismaticsItems.ID_CARDS.get(color))
        .unlockedBy(Ingredients::precisionMechanism)
        .viaShaped(b -> b
            .pattern(" / ")
            .pattern("_-,")
            .define(',', Ingredients.brassNugget())
            .define('-', Ingredients.paper())
            .define('_', Ingredients.ironSheet())
            .define('/', Ingredients.dye(color)))
    );

    GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
        return new GeneratedRecipeBuilder("/", result);
    }

    GeneratedRecipeBuilder create(ResourceLocation result) {
        return new GeneratedRecipeBuilder("/", result);
    }

    GeneratedRecipeBuilder create(ItemProviderEntry<? extends ItemLike> result) {
        return create(result::get);
    }

    public NumismaticsStandardRecipeGen(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    public String getName() {
        return "Numismatics Standard Recipes";
    }

    class GeneratedRecipeBuilder {

        private final String path;
        private String suffix;
        private Supplier<? extends ItemLike> result;
        private ResourceLocation compatDatagenOutput;

        private Supplier<ItemPredicate> unlockedBy;
        private int amount;

        private GeneratedRecipeBuilder(String path) {
            this.path = path;
            this.suffix = "";
            this.amount = 1;
        }

        public GeneratedRecipeBuilder(String path, Supplier<? extends ItemLike> result) {
            this(path);
            this.result = result;
        }

        public GeneratedRecipeBuilder(String path, ResourceLocation result) {
            this(path);
            this.compatDatagenOutput = result;
        }

        GeneratedRecipeBuilder returns(int amount) {
            this.amount = amount;
            return this;
        }

        GeneratedRecipeBuilder unlockedBy(Supplier<? extends ItemLike> item) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                .of(item.get())
                .build();
            return this;
        }

        GeneratedRecipeBuilder unlockedByTag(Supplier<TagKey<Item>> tag) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                .of(tag.get())
                .build();
            return this;
        }

        GeneratedRecipeBuilder withSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
            return register(consumer -> {
                ShapedRecipeBuilder b = builder.apply(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(consumer, createLocation("crafting"));
            });
        }

        GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
            return register(consumer -> {
                ShapelessRecipeBuilder b = builder.apply(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(consumer, createLocation("crafting"));
            });
        }

        private ResourceLocation createSimpleLocation(String recipeType) {
            return Numismatics.asResource(recipeType + "/" + getRegistryName().getPath() + suffix);
        }

        private ResourceLocation createLocation(String recipeType) {
            return Numismatics.asResource(recipeType + "/" + path + "/" + getRegistryName().getPath() + suffix);
        }

        private ResourceLocation getRegistryName() {
            return compatDatagenOutput == null ? RegisteredObjects.getKeyOrThrow(result.get()
                .asItem()) : compatDatagenOutput;
        }

        GeneratedCookingRecipeBuilder viaCooking(Supplier<? extends ItemLike> item) {
            return unlockedBy(item).viaCookingIngredient(() -> Ingredient.of(item.get()));
        }

        GeneratedCookingRecipeBuilder viaCookingTag(Supplier<TagKey<Item>> tag) {
            return unlockedByTag(tag).viaCookingIngredient(() -> Ingredient.of(tag.get()));
        }

        GeneratedCookingRecipeBuilder viaCookingIngredient(Supplier<Ingredient> ingredient) {
            return new GeneratedCookingRecipeBuilder(ingredient);
        }

        class GeneratedCookingRecipeBuilder {

            private final Supplier<Ingredient> ingredient;
            private float exp;
            private int cookingTime;

            private final SimpleCookingSerializer<?> FURNACE = (SimpleCookingSerializer<?>) RecipeSerializer.SMELTING_RECIPE,
                SMOKER = (SimpleCookingSerializer<?>) RecipeSerializer.SMOKING_RECIPE, BLAST = (SimpleCookingSerializer<?>) RecipeSerializer.BLASTING_RECIPE,
                CAMPFIRE = (SimpleCookingSerializer<?>) RecipeSerializer.CAMPFIRE_COOKING_RECIPE;

            GeneratedCookingRecipeBuilder(Supplier<Ingredient> ingredient) {
                this.ingredient = ingredient;
                cookingTime = 200;
                exp = 0;
            }

            GeneratedCookingRecipeBuilder forDuration(int duration) {
                cookingTime = duration;
                return this;
            }

            GeneratedCookingRecipeBuilder rewardXP(float xp) {
                exp = xp;
                return this;
            }

            GeneratedRecipe inFurnace() {
                return inFurnace(b -> b);
            }

            GeneratedRecipe inFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                return create(FURNACE, builder, 1);
            }

            GeneratedRecipe inSmoker() {
                return inSmoker(b -> b);
            }

            GeneratedRecipe inSmoker(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                create(FURNACE, builder, 1);
                create(CAMPFIRE, builder, 3);
                return create(SMOKER, builder, .5f);
            }

            GeneratedRecipe inBlastFurnace() {
                return inBlastFurnace(b -> b);
            }

            GeneratedRecipe inBlastFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                create(FURNACE, builder, 1);
                return create(BLAST, builder, .5f);
            }

            private GeneratedRecipe create(SimpleCookingSerializer<?> serializer,
                                           UnaryOperator<SimpleCookingRecipeBuilder> builder, float cookingTimeModifier) {
                return register(consumer -> {
                    boolean isOtherMod = compatDatagenOutput != null;

                    SimpleCookingRecipeBuilder b = builder.apply(
                        SimpleCookingRecipeBuilder.generic(ingredient.get(), RecipeCategory.MISC, isOtherMod ? Items.DIRT : result.get(),
                            exp, (int) (cookingTime * cookingTimeModifier), serializer));
                    if (unlockedBy != null)
                        b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                    b.save(result -> {
                        consumer.accept(result);
                    }, createSimpleLocation(RegisteredObjects.getKeyOrThrow(serializer)
                        .getPath()));
                });
            }
        }
    }
}
