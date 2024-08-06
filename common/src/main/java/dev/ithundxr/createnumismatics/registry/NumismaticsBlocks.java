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

import com.simibubi.create.AllBlocks;
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
import dev.ithundxr.createnumismatics.content.salepoint.SalepointBlock;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointBlockItem;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlock;
import dev.ithundxr.createnumismatics.multiloader.CommonTags;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class NumismaticsBlocks {
	private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

	public static final BlockEntry<AndesiteDepositorBlock> ANDESITE_DEPOSITOR = REGISTRATE.block("andesite_depositor", AndesiteDepositorBlock::new)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.properties(p -> p.sound(SoundType.WOOD))
		.properties(p -> p.strength(1.0F, 3600000.0F)) // explosion resistance same as bedrock
		.properties(p -> p.isRedstoneConductor((state, getter, pos) -> false))
		.transform(axeOrPickaxe())
		.tag(CommonTags.RELOCATION_NOT_SUPPORTED.tag)
		.lang("Andesite Depositor")
		.transform(BuilderTransformers.depositor("andesite"))
		.simpleItem()
		.register();

	public static final BlockEntry<BrassDepositorBlock> BRASS_DEPOSITOR = REGISTRATE.block("brass_depositor", BrassDepositorBlock::new)
		.properties(p -> p.mapColor(MapColor.PODZOL))
		.properties(p -> p.sound(SoundType.WOOD))
		.properties(p -> p.strength(1.4F, 3600000.0F)) // explosion resistance same as bedrock
		.properties(p -> p.isRedstoneConductor((state, getter, pos) -> false))
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
		.properties(p -> p.strength(1.0F,3600000.0F)) // Unexplodable
		.properties(Properties::requiresCorrectToolForDrops)
		.transform(pickaxeOnly())
		.lang("Bank Terminal")
		.transform(BuilderTransformers.bankTerminal())
		.simpleItem()
		.register();

	public static final BlockEntry<BlazeBankerBlock> BLAZE_BANKER = REGISTRATE.block("blaze_banker", BlazeBankerBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY))
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.properties(p -> p.lightLevel(state -> 15))
		.properties(p -> p.strength(1.0F,3600000.0F)) // Unexplodable
		.properties(Properties::requiresCorrectToolForDrops)
		.transform(pickaxeOnly())
		.transform(BuilderTransformers.blazeBanker())
		.addLayer(() -> RenderType::cutoutMipped)
		.tag(CommonTags.RELOCATION_NOT_SUPPORTED.tag)
		.tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag, AllTags.AllBlockTags.PASSIVE_BOILER_HEATERS.tag)
		.loot((lt, block) -> lt.add(block, lt.createSingleItemTable(AllBlocks.BLAZE_BURNER)
			.withPool(lt.applyExplosionCondition(NumismaticsItems.BANKING_GUIDE.get(), LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1.0f))
				.add(LootItem.lootTableItem(NumismaticsItems.BANKING_GUIDE.get()))))))
		.lang("Blaze Banker")
		.simpleItem()
		.register();

	public static final BlockEntry<VendorBlock> VENDOR = REGISTRATE.block("vendor", p -> new VendorBlock(p, false))
		.initialProperties(SharedProperties::softMetal)
		.properties(Properties::noOcclusion)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.properties(p -> p.strength(1.0F, 3600000.0F)) // Unexplodable
		.properties(Properties::requiresCorrectToolForDrops)
		.transform(pickaxeOnly())
		.addLayer(() -> RenderType::cutout)
		.lang("Vendor")
		.transform(BuilderTransformers.vendor(false))
		.item()
		.transform(BuilderTransformers.vendorItem(false))
		.build()
		.register();

	public static final BlockEntry<VendorBlock> CREATIVE_VENDOR = REGISTRATE.block("creative_vendor", p -> new VendorBlock(p, true))
		.initialProperties(SharedProperties::softMetal)
		.properties(Properties::noOcclusion)
		.properties(p -> p.mapColor(MapColor.TERRACOTTA_WHITE))
		.properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
		.properties(p -> p.strength(-1.0F, 3600000.0F)) // Unbreakable & Unexplodable
		.properties(Properties::requiresCorrectToolForDrops)
		.transform(pickaxeOnly())
		.addLayer(() -> RenderType::cutout)
		.lang("Creative Vendor")
		.transform(BuilderTransformers.vendor(true))
		.item()
		.properties(p -> p.rarity(Rarity.EPIC))
		.transform(BuilderTransformers.vendorItem(true))
		.build()
		.register();

	public static final BlockEntry<SalepointBlock> SALEPOINT = REGISTRATE.block("salepoint", SalepointBlock::new)
		.initialProperties(SharedProperties::softMetal)
		.properties(p -> p.strength(1.0F, 3600000.0F)) // Unexplodable
		.properties(Properties::requiresCorrectToolForDrops)
		.properties(p -> p.isRedstoneConductor(Blocks::never))
		.transform(pickaxeOnly())
		.lang("Salepoint")
		//.transform(BuilderTransformers.salepoint())
		.item(SalepointBlockItem::new)
		.build()
		.register();

	public static void register() {
		// load the class and register everything
		Numismatics.LOGGER.info("Registering blocks for " + Numismatics.NAME);
	}
}
