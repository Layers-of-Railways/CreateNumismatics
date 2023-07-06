package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.util.ItemUtils;
import net.minecraft.world.item.*;
import javax.annotation.Nonnull;

public class NumismaticsItems {
	private static final CreateRegistrate REGISTRATE = Numismatics.registrate();
	public static final CreativeModeTab mainCreativeTab = new CreativeModeTab(ItemUtils.nextTabId(), Numismatics.MOD_ID) {
		@Override
		@Nonnull
		public ItemStack makeIcon() { return NumismaticsItems.EXAMPLE_ITEM.asStack(); }
	};

	public static final ItemEntry<CoinItem> SPUR = REGISTRATE.item("spur", CoinItem.create(Coin.SPUR)).lang("Spur").register();
	public static final ItemEntry<CoinItem> BEVEL = REGISTRATE.item("bevel", CoinItem.create(Coin.BEVEL)).lang("Bevel").register();
	public static final ItemEntry<CoinItem> SPROCKET = REGISTRATE.item("sprocket", CoinItem.create(Coin.SPROCKET)).lang("Sprocket").register();
	public static final ItemEntry<CoinItem> COG = REGISTRATE.item("cog", CoinItem.create(Coin.COG)).lang("Cog").register();
	public static final ItemEntry<CoinItem> CROWN = REGISTRATE.item("crown", CoinItem.create(Coin.CROWN)).lang("Crown").register();
	public static final ItemEntry<CoinItem> SUN = REGISTRATE.item("sun", CoinItem.create(Coin.SUN)).lang("Sun").register();

	public static void init() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering items for " + Numismatics.NAME);
	}
}
