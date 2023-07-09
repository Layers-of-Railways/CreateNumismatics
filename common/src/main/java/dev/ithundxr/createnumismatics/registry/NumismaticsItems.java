package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.item.DyedItemList;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.util.ItemUtils;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.CoinItem;
import dev.ithundxr.createnumismatics.util.TextUtils;
import net.minecraft.world.item.*;
import javax.annotation.Nonnull;
import java.util.EnumMap;

public class NumismaticsItems {
	private static final CreateRegistrate REGISTRATE = Numismatics.registrate();
	public static final CreativeModeTab mainCreativeTab = new CreativeModeTab(ItemUtils.nextTabId(), Numismatics.MOD_ID) {
		@Override
		@Nonnull
		public ItemStack makeIcon() { return getCoin(Coin.COG).asStack(); }
	};

	private static ItemEntry<CoinItem> makeCoin(Coin coin) {
		return REGISTRATE.item(coin.getName(), CoinItem.create(coin))
			.tag(NumismaticsTags.AllItemTags.COINS.tag)
			.lang(coin.getDisplayName())
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
			.register();
	});

	public static void register() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering items for " + Numismatics.NAME);
	}
}
