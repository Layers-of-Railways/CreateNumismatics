package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlock;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.client.model.generators.ConfiguredModel;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class NumismaticsBlocks {
	private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

	public static final BlockEntry<Block> BANK_TERMINAL = REGISTRATE.block("bank_terminal", Block::new).register();

	public static final BlockEntry<AndesiteDepositorBlock> ANDESITE_DEPOSITOR = REGISTRATE.block("andesite_depositor", AndesiteDepositorBlock::new)
		.properties(p -> p.color(MaterialColor.PODZOL))
		.properties(p -> p.sound(SoundType.WOOD))
		.properties(p -> p.explosionResistance(3600000.0f)) // same as bedrock
		.transform(axeOrPickaxe())
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStatesExcept((state) -> ConfiguredModel.builder()
					.modelFile(p.models()
						.cube(c.getName(), p.modLoc("block/andesite_depositor_select"), p.modLoc("block/andesite_depositor_select"),
							p.modLoc("block/andesite_depositor_side"), p.modLoc("block/andesite_depositor_slot"),
							p.modLoc("block/andesite_depositor_side"), p.modLoc("block/andesite_depositor_side"))
					)
					.rotationY((int) state.getValue(AbstractDepositorBlock.HORIZONTAL_FACING).toYRot())
					.build(),
				AbstractDepositorBlock.POWERED
			)
		)
		.simpleItem()
		.register();

	public static void init() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering blocks for " + Numismatics.NAME);
	}
}
