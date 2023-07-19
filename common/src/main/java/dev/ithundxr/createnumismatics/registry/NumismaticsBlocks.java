package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlock;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlock;
import dev.ithundxr.createnumismatics.content.bank.BankTerminalBlock;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlock;
import dev.ithundxr.createnumismatics.multiloader.CommonTags;
import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class NumismaticsBlocks {
	private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

	public static final BlockEntry<AndesiteDepositorBlock> ANDESITE_DEPOSITOR = REGISTRATE.block("andesite_depositor", AndesiteDepositorBlock::new)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.properties(p -> p.sound(SoundType.WOOD))
		.properties(p -> p.strength(1.0f, 3600000.0f)) // explosion resistance same as bedrock
		.transform(axeOrPickaxe())
		.tag(CommonTags.RELOCATION_NOT_SUPPORTED.tag)
		.lang("Andesite Depositor")
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStatesExcept((state) -> ConfiguredModel.builder()
					.modelFile(p.models()
						.orientable(c.getName() + (state.getValue(AbstractDepositorBlock.LOCKED) ? "_locked" : ""),
							Create.asResource("block/andesite_casing"),
							p.modLoc("block/andesite_depositor_slot" + (state.getValue(AbstractDepositorBlock.LOCKED) ? "_locked" : "")),
							p.modLoc("block/andesite_depositor_select"))
						.texture("particle", Create.asResource("block/andesite_casing"))
					)
					.rotationY((int) state.getValue(AbstractDepositorBlock.HORIZONTAL_FACING).toYRot() + 180)
					.build(),
				AbstractDepositorBlock.POWERED
			)
		)
		.simpleItem()
		.register();

	public static final BlockEntry<BrassDepositorBlock> BRASS_DEPOSITOR = REGISTRATE.block("brass_depositor", BrassDepositorBlock::new)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.properties(p -> p.sound(SoundType.WOOD))
		.properties(p -> p.strength(1.4f, 3600000.0f)) // explosion resistance same as bedrock
		.transform(axeOrPickaxe())
		.tag(CommonTags.RELOCATION_NOT_SUPPORTED.tag)
		.lang("Brass Depositor")
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStatesExcept((state) -> ConfiguredModel.builder()
					.modelFile(p.models()
						.orientable(c.getName() + (state.getValue(AbstractDepositorBlock.LOCKED) ? "_locked" : ""),
							Create.asResource("block/brass_casing"),
							p.modLoc("block/brass_depositor_slot" + (state.getValue(AbstractDepositorBlock.LOCKED) ? "_locked" : "")),
							p.modLoc("block/brass_depositor_select"))
						.texture("particle", Create.asResource("block/brass_casing"))
					)
					.rotationY((int) state.getValue(AbstractDepositorBlock.HORIZONTAL_FACING).toYRot() + 180)
					.build(),
				AbstractDepositorBlock.POWERED
			)
		)
		.simpleItem()
		.register();

	public static final BlockEntry<BankTerminalBlock> BANK_TERMINAL = REGISTRATE.block("bank_terminal", BankTerminalBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY))
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
		.transform(pickaxeOnly())
		.lang("Bank Terminal")
		.blockstate((c, p) -> p.getVariantBuilder(c.get())
			.forAllStates((state) -> ConfiguredModel.builder()
				.modelFile(p.models()
					.orientable(c.getName(), p.modLoc("block/bank_terminal_side"), p.modLoc("block/bank_terminal_front"),
						p.modLoc("block/bank_terminal_top"))
					.texture("particle", p.modLoc("block/bank_terminal_top"))
				)
				.rotationY((int) state.getValue(BankTerminalBlock.HORIZONTAL_FACING).toYRot() + 180)
				.build()
			)
		)
		.simpleItem()
		.register();

	public static void register() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering blocks for " + Numismatics.NAME);
	}
}
