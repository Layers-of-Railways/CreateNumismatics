package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class NumismaticsItems {
	private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

	public static final ItemEntry<Item> EXAMPLE_ITEM = REGISTRATE.item("example_item", Item::new).register();
	public static final ItemEntry<Item> SPUR = REGISTRATE.item("spur", Item::new).lang("Spur").register();
	public static final ItemEntry<Item> BEVEL = REGISTRATE.item("bevel", Item::new).lang("Bevel").register();
	public static final ItemEntry<Item> SPROCKET = REGISTRATE.item("sprocket", Item::new).lang("Sprocket").register();
	public static final ItemEntry<Item> COG = REGISTRATE.item("cog", Item::new).lang("Cog").register();
	public static final ItemEntry<Item> CROWN = REGISTRATE.item("crown", Item::new).lang("Crown").register();
	public static final ItemEntry<Item> SUN = REGISTRATE.item("sun", Item::new).lang("Sun").register();

	public static void init() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering items for " + Numismatics.NAME);
	}
}
