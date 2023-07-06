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

	public static final ItemEntry<Item> EXAMPLE_ITEM = REGISTRATE.item("example_item", Item::new).register();

	public static void init() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering items for " + Numismatics.NAME);
	}
}
