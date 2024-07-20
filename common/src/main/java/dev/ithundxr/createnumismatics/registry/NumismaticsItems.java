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

package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.item.DyedItemList;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.bank.AuthorizedCardItem;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BankingGuideItem;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.core.registries.Registries;

import java.util.EnumMap;

public class NumismaticsItems {
	private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

	private static ItemEntry<CoinItem> makeCoin(Coin coin) {
		return REGISTRATE.item(coin.getName(), CoinItem.create(coin))
			.tag(NumismaticsTags.AllItemTags.COINS.tag)
			.lang(coin.getDefaultLangName())
			.properties(p -> p.rarity(coin.rarity))
			.model((c, p) -> p.generated(c, p.modLoc("item/coin/" + coin.getName())))
			.register();
	}

	public static final EnumMap<Coin, ItemEntry<CoinItem>> COINS = new EnumMap<>(Coin.class);

	static {
		for (Coin coin : Coin.values()) {
			COINS.put(coin, makeCoin(coin));
		}
	}

	public static ItemEntry<CoinItem> getCoin(Coin coin) {
		return COINS.get(coin);
	}

	public static final DyedItemList<CardItem> CARDS = new DyedItemList<>(color -> {
		String colorName = color.getSerializedName();
		return REGISTRATE.item(colorName+"_card", p -> new CardItem(p, color))
			.properties(p -> p.stacksTo(1))
			.tag(NumismaticsTags.AllItemTags.CARDS.tag)
			.lang(TextUtils.titleCaseConversion(color.getName()) + " Card")
			.model((c, p) -> p.generated(c, Numismatics.asResource("item/card/"+colorName+"_card")))
			.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "item.numismatics.bank_card"))
			.register();
	});

	public static final DyedItemList<IDCardItem> ID_CARDS = new DyedItemList<>(color -> {
		String colorName = color.getSerializedName();
		return REGISTRATE.item(colorName+"_id_card", p -> new IDCardItem(p, color))
			.properties(p -> p.stacksTo(16))
			.tag(NumismaticsTags.AllItemTags.ID_CARDS.tag)
			.lang(TextUtils.titleCaseConversion(color.getName()) + " ID Card")
			.model((c, p) -> p.generated(c, Numismatics.asResource("item/id_card/"+colorName+"_id_card")))
			.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "item.numismatics.id_card"))
			.register();
	});

	public static final DyedItemList<AuthorizedCardItem> AUTHORIZED_CARDS = new DyedItemList<>(color -> {
		String colorName = color.getSerializedName();
		return REGISTRATE.item(colorName+"_authorized_card", p -> new AuthorizedCardItem(p, color))
			.properties(p -> p.stacksTo(1))
			.tag(NumismaticsTags.AllItemTags.AUTHORIZED_CARDS.tag)
			.lang(TextUtils.titleCaseConversion(color.getName()) + " Authorized Card")
			.model((c, p) -> p.generated(c, Numismatics.asResource("item/authorized_card/"+colorName+"_authorized_card")))
			.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "item.numismatics.authorized_bank_card"))
			.register();
	});

	public static final ItemEntry<BankingGuideItem> BANKING_GUIDE = REGISTRATE.item("banking_guide", BankingGuideItem::new)
		.lang("Banking Guide")
		.register();

	public static void register() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering items for " + Numismatics.NAME);
	}
}
