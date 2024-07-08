/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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

package dev.ithundxr.createnumismatics.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import dev.ithundxr.createnumismatics.config.CommonModConfig;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Paths;

public class ForgeModConfig {
	private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

	public static final ForgeConfigSpec.IntValue STARTER_SPUR;
	public static final ForgeConfigSpec.IntValue STARTER_BEVEL;
	public static final ForgeConfigSpec.IntValue STARTER_SPROCKET;
	public static final ForgeConfigSpec.IntValue STARTER_COG;
	public static final ForgeConfigSpec.IntValue STARTER_CROWN;
	public static final ForgeConfigSpec.IntValue STARTER_SUN;

	public static final ForgeConfigSpec.IntValue SPUR_VALUE;
	public static final ForgeConfigSpec.IntValue BEVEL_VALUE;
	public static final ForgeConfigSpec.IntValue SPROCKET_VALUE;
	public static final ForgeConfigSpec.IntValue COG_VALUE;
	public static final ForgeConfigSpec.IntValue CROWN_VALUE;
	public static final ForgeConfigSpec.IntValue SUN_VALUE;

	public static final ForgeConfigSpec.ConfigValue<String> CURRENCY;

	static {
		COMMON_BUILDER.comment("Numismatics configuration file").push("general");
		CURRENCY     = COMMON_BUILDER.comment("The default currency").define("currency", "COG");

		COMMON_BUILDER.pop();
		COMMON_BUILDER.comment("The number of coins added when a player first looks at their bank account").push("starter");
		STARTER_SPUR     = COMMON_BUILDER.comment("The number of spurs added when a player first looks at their bank account").defineInRange("spur", 0, 0, Integer.MAX_VALUE);
		STARTER_BEVEL    = COMMON_BUILDER.comment("The number of bevels added when a player first looks at their bank account").defineInRange("bevel", 0, 0, Integer.MAX_VALUE);
		STARTER_SPROCKET = COMMON_BUILDER.comment("The number of sprockets added when a player first looks at their bank account").defineInRange("sprocket", 0, 0, Integer.MAX_VALUE);
		STARTER_COG      = COMMON_BUILDER.comment("The number of cogs added when a player first looks at their bank account").defineInRange("cog", 0, 0, Integer.MAX_VALUE);
		STARTER_CROWN    = COMMON_BUILDER.comment("The number of crowns added when a player first looks at their bank account").defineInRange("crown", 0, 0, Integer.MAX_VALUE);
		STARTER_SUN      = COMMON_BUILDER.comment("The number of suns added when a player first looks at their bank account").defineInRange("sun", 0, 0, Integer.MAX_VALUE);

		COMMON_BUILDER.pop();
		COMMON_BUILDER.comment("The value of each coins in spurs").push("value");
		SPUR_VALUE     = COMMON_BUILDER.defineInRange("spur", 1, 0, Integer.MAX_VALUE);
		BEVEL_VALUE    = COMMON_BUILDER.defineInRange("bevel", 8, 0, Integer.MAX_VALUE);
		SPROCKET_VALUE = COMMON_BUILDER.defineInRange("sprocket", 16, 0, Integer.MAX_VALUE);
		COG_VALUE      = COMMON_BUILDER.defineInRange("cog", 64, 0, Integer.MAX_VALUE);
		CROWN_VALUE    = COMMON_BUILDER.defineInRange("crown", 512, 0, Integer.MAX_VALUE);
		SUN_VALUE      = COMMON_BUILDER.defineInRange("sun", 4096, 0, Integer.MAX_VALUE);

		COMMON_BUILDER.pop();

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_BUILDER.build());
	}

	public static void loadConfig() {
		final CommentedFileConfig configData = CommentedFileConfig.builder(Paths.get("config/" + "numismatics" + "-common.toml"))
				.sync()
				.autosave()
				.writingMode(WritingMode.REPLACE)
				.build();
		configData.load();
		COMMON_BUILDER.build().setConfig(configData);

		CommonModConfig.starterSpur  = STARTER_SPUR.get();
		CommonModConfig.starterBevel = STARTER_BEVEL.get();
		CommonModConfig.starterSprocket = STARTER_SPROCKET.get();
		CommonModConfig.starterCog = STARTER_COG.get();
		CommonModConfig.starterCrown = STARTER_CROWN.get();
		CommonModConfig.starterSun = STARTER_SUN.get();

		Coin.SPUR.setValue(SPUR_VALUE.get());
		Coin.BEVEL.setValue(BEVEL_VALUE.get());
		Coin.SPROCKET.setValue(SPROCKET_VALUE.get());
		Coin.COG.setValue(COG_VALUE.get());
		Coin.CROWN.setValue(CROWN_VALUE.get());
		Coin.SUN.setValue(SUN_VALUE.get());

		CommonModConfig.currency  = Coin.valueOf(CURRENCY.get());
	}
}
