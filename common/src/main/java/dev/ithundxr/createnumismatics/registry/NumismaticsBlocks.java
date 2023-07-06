package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import net.minecraft.world.level.block.Block;

public class NumismaticsBlocks {
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(Numismatics.MOD_ID);

	public static final BlockEntry<Block> BANK_TERMINAL = REGISTRATE.block("bank_terminal", Block::new).register();

	public static void init() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering blocks for " + Numismatics.NAME);
	}
}
