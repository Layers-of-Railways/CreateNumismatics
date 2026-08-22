/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.registry;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.AuthorizedCardItem;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
import net.createmod.ponder.api.registration.IndexExclusionHelper;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

public class NumismaticsPonderPlugin implements PonderPlugin {
	@Override
	public @NotNull String getModId() {
		return Numismatics.MOD_ID;
	}

	@Override
	public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
		NumismaticsPonderScenes.register(helper);
	}

	@Override
	public void registerTags(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
		NumismaticsPonderTags.register(helper);
	}

	@Override
	public void registerSharedText(@NotNull SharedTextRegistrationHelper helper) {
		PonderPlugin.super.registerSharedText(helper);

		// Add entries used across several ponder scenes (Safe for hotswap)

		for (int i = 1; i < 10; i++) {
			helper.registerSharedText("amount" + i + "x", i + "x");
			helper.registerSharedText("amount_spaced_" + i + "x", i + "x ");
		}
	}

	@Override
	public void indexExclusions(@NotNull IndexExclusionHelper helper) {
		helper.excludeItemVariants(CardItem.class, NumismaticsItems.CARDS.get(DyeColor.RED).get());
		helper.excludeItemVariants(AuthorizedCardItem.class, NumismaticsItems.AUTHORIZED_CARDS.get(DyeColor.RED).get());
		helper.excludeItemVariants(IDCardItem.class, NumismaticsItems.ID_CARDS.get(DyeColor.RED).get());
	}
}