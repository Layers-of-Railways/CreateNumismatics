package dev.ithundxr.createnumismatics;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class NumismaticsItems {
	private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

	public static final ItemEntry<Item> EXAMPLE_ITEM = REGISTRATE.item("example_item", Item::new).register();

	public static void init() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering items for " + Numismatics.NAME);
	}
}
