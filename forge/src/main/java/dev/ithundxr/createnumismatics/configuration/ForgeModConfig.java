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

	static {
		COMMON_BUILDER.comment("Numismatics configuration file").push("starter");
		STARTER_SPUR     = COMMON_BUILDER.comment("The number of spurs added when a player first looks at their bank account").defineInRange("spur", 0, 0, Integer.MAX_VALUE);
		STARTER_BEVEL    = COMMON_BUILDER.comment("The number of bevels added when a player first looks at their bank account").defineInRange("bevel", 0, 0, Integer.MAX_VALUE);
		STARTER_SPROCKET = COMMON_BUILDER.comment("The number of sprockets added when a player first looks at their bank account").defineInRange("sprocket", 0, 0, Integer.MAX_VALUE);
		STARTER_COG      = COMMON_BUILDER.comment("The number of cogs added when a player first looks at their bank account").defineInRange("cog", 0, 0, Integer.MAX_VALUE);
		STARTER_CROWN    = COMMON_BUILDER.comment("The number of crowns added when a player first looks at their bank account").defineInRange("crown", 0, 0, Integer.MAX_VALUE);
		STARTER_SUN      = COMMON_BUILDER.comment("The number of suns added when a player first looks at their bank account").defineInRange("sun", 0, 0, Integer.MAX_VALUE);
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
	}
}
