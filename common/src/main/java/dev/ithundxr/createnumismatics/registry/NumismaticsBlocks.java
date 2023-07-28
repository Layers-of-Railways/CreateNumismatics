package dev.ithundxr.createnumismatics.registry;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.base.data.BuilderTransformers;
import dev.ithundxr.createnumismatics.content.bank.BankTerminalBlock;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerBlock;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlock;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlock;
import dev.ithundxr.createnumismatics.multiloader.CommonTags;
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
		.transform(BuilderTransformers.depositor("andesite"))
		.simpleItem()
		.register();

	public static final BlockEntry<BrassDepositorBlock> BRASS_DEPOSITOR = REGISTRATE.block("brass_depositor", BrassDepositorBlock::new)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.properties(p -> p.sound(SoundType.WOOD))
		.properties(p -> p.strength(1.4f, 3600000.0f)) // explosion resistance same as bedrock
		.transform(axeOrPickaxe())
		.tag(CommonTags.RELOCATION_NOT_SUPPORTED.tag)
		.lang("Brass Depositor")
		.transform(BuilderTransformers.depositor("brass"))
		.simpleItem()
		.register();

	public static final BlockEntry<BankTerminalBlock> BANK_TERMINAL = REGISTRATE.block("bank_terminal", BankTerminalBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_GRAY))
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
		.transform(pickaxeOnly())
		.lang("Bank Terminal")
		.transform(BuilderTransformers.bankTerminal())
		.simpleItem()
		.register();

	public static final BlockEntry<BlazeBankerBlock> BLAZE_BANKER = REGISTRATE.block("blaze_banker", BlazeBankerBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY))
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
		.transform(pickaxeOnly())
		.tag(CommonTags.RELOCATION_NOT_SUPPORTED.tag)
		.tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag, AllTags.AllBlockTags.PASSIVE_BOILER_HEATERS.tag)
		.lang("Blaze Banker")
//		.transform(BuilderTransformers.bankTerminal())
		.simpleItem()
		.register();

	public static void register() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering blocks for " + Numismatics.NAME);
	}
}
