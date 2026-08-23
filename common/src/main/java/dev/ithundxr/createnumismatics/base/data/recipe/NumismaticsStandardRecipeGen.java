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

package dev.ithundxr.createnumismatics.base.data.recipe;

import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.data.recipe.Ingredients;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class NumismaticsStandardRecipeGen extends BaseRecipeProvider {

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

    GeneratedRecipe SALEPOINT = create(NumismaticsBlocks.SALEPOINT)
        .unlockedBy(Ingredients::vendor)
        .viaShaped(b -> b
            .pattern(" p ")
            .pattern("IvI")
            .pattern(" @ ")
            .define('p', Ingredients.placard())
            .define('I', Ingredients.electronTube())
            .define('v', Ingredients.vendor())
            .define('@', Ingredients.precisionMechanism()));

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

    DyedRecipeList AUTHORIZED_CARDS = new DyedRecipeList(color -> create(NumismaticsItems.AUTHORIZED_CARDS.get(color))
        .unlockedBy(Ingredients::precisionMechanism)
        .viaShaped(b -> b
            .pattern("@_/")
            .define('@', Ingredients.precisionMechanism())
            .define('_', Ingredients.goldSheet())
            .define('/', Ingredients.dye(color)))
    );

    GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
        return new GeneratedRecipeBuilder("/", result);
    }

    GeneratedRecipeBuilder create(ResourceLocation result) {
        return new GeneratedRecipeBuilder("/", result);
    }

    GeneratedRecipeBuilder create(ItemProviderEntry<? extends ItemLike, ?> result) {
        return create(result::get);
    }

    public NumismaticsStandardRecipeGen(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, Numismatics.MOD_ID);
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
            return compatDatagenOutput == null ? RegisteredObjectsHelper.getKeyOrThrow(result.get()
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
                return create(RecipeSerializer.SMELTING_RECIPE, builder, SmeltingRecipe::new, 1);
            }

            GeneratedRecipe inSmoker() {
                return inSmoker(b -> b);
            }

            GeneratedRecipe inSmoker(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                create(RecipeSerializer.SMELTING_RECIPE, builder, SmeltingRecipe::new, 1);
                create(RecipeSerializer.CAMPFIRE_COOKING_RECIPE, builder, CampfireCookingRecipe::new, 3);
                return create(RecipeSerializer.SMOKING_RECIPE, builder, SmokingRecipe::new, .5f);
            }

            GeneratedRecipe inBlastFurnace() {
                return inBlastFurnace(b -> b);
            }

            GeneratedRecipe inBlastFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                create(RecipeSerializer.SMELTING_RECIPE, builder, SmeltingRecipe::new, 1);
                return create(RecipeSerializer.BLASTING_RECIPE, builder, BlastingRecipe::new, .5f);
            }

            private <T extends AbstractCookingRecipe> GeneratedRecipe create(RecipeSerializer<T> serializer,
                                           UnaryOperator<SimpleCookingRecipeBuilder> builder, AbstractCookingRecipe.Factory<T> factory,  float cookingTimeModifier) {
                return register(consumer -> {
                    boolean isOtherMod = compatDatagenOutput != null;

                    SimpleCookingRecipeBuilder b = builder.apply(SimpleCookingRecipeBuilder.generic(ingredient.get(),
                            RecipeCategory.MISC, isOtherMod ? Items.DIRT : result.get(), exp,
                            (int) (cookingTime * cookingTimeModifier), serializer, factory));
                    if (unlockedBy != null)
                        b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                    b.save(consumer, createSimpleLocation(RegisteredObjectsHelper.getKeyOrThrow(serializer)
                        .getPath()));
                });
            }
        }
    }
}
