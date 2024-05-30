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

package dev.ithundxr.createnumismatics.registry;

import com.google.common.collect.Sets;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.registry.advancement.NumismaticsAdvancement;
import dev.ithundxr.createnumismatics.registry.advancement.NumismaticsAdvancement.Builder;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static dev.ithundxr.createnumismatics.registry.advancement.NumismaticsAdvancement.TaskType.SECRET;
import static dev.ithundxr.createnumismatics.registry.advancement.NumismaticsAdvancement.TaskType.SILENT;

public class NumismaticsAdvancements implements DataProvider {

	public static final List<NumismaticsAdvancement> ENTRIES = new ArrayList<>();
	public static final NumismaticsAdvancement START = null,

	/*
	 * Some ids have trailing 0's to modify their vertical position on the tree
	 * (Advancement ordering seems to be deterministic but hash based)
	 */

	ROOT = create("root", b -> b.icon(Coin.CROWN.asStack())
		.title("Welcome to Numismatics")
		.description("Here Be Riches")
		.awardedForFree()
		.special(SILENT)),

	// Special advancements

	MONEY_LAUNDERING = create("money_laundering", b -> b.icon(Items.WATER_BUCKET)
		.title("Money Laundering")
		.description("Buy coins in a vendor")
		.after(ROOT)
		.special(SECRET)
	),

	QUESTIONABLE_INVESTMENT = create("questionable_investment", b -> b.icon(Coin.SPUR.asStack())
		.title("Questionable Investment")
		.description("Buy coins for more than they are worth")
		.after(MONEY_LAUNDERING)
		.special(SECRET)
	),

	IS_THIS_LEGAL = create("is_this_legal", b -> b.icon(Coin.SUN.asStack())
		.title("Is This Legal?")
		.description("Buy coins for less than they are worth")
		.after(MONEY_LAUNDERING)
		.special(SECRET)
	),

	//
	END = null;

	private static NumismaticsAdvancement create(String id, UnaryOperator<Builder> b) {
		return new NumismaticsAdvancement(id, b);
	}

	// Datagen

	private final PackOutput output;

	public NumismaticsAdvancements(PackOutput output) {
		this.output = output;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		PathProvider pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancements");
		List<CompletableFuture<?>> futures = new ArrayList<>();

		Set<ResourceLocation> set = Sets.newHashSet();
		Consumer<Advancement> consumer = (advancement) -> {
			ResourceLocation id = advancement.getId();
			if (!set.add(id))
				throw new IllegalStateException("Duplicate advancement " + id);
			Path path = pathProvider.json(id);
			futures.add(DataProvider.saveStable(cache, advancement.deconstruct()
				.serializeToJson(), path));
		};

		for (NumismaticsAdvancement advancement : ENTRIES)
			advancement.save(consumer);

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	@Override
	public String getName() {
		return "Numismatics' Advancements";
	}

	public static void provideLang(BiConsumer<String, String> consumer) {
		for (NumismaticsAdvancement advancement : ENTRIES)
			advancement.provideLang(consumer);
	}

	public static void register() {}

}
