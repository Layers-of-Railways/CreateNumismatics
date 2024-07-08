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
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricModConfig {
	public static void loadConfig() {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve("numismatics-common.toml");
		CommentedFileConfig configData = CommentedFileConfig.builder(configPath)
				.sync()
				.autosave()
				.writingMode(WritingMode.REPLACE)
				.build();
		configData.load();

		if (!configData.contains("starter.spur")) configData.set("starter.spur", 0);
		if (!configData.contains("starter.bevel")) configData.set("starter.bevel", 0);
		if (!configData.contains("starter.sprocket")) configData.set("starter.sprocket", 0);
		if (!configData.contains("starter.cog")) configData.set("starter.cog", 0);
		if (!configData.contains("starter.crown")) configData.set("starter.crown", 0);
		if (!configData.contains("starter.sun")) configData.set("starter.sun", 0);

		CommonModConfig.starterSpur  = configData.get("starter.spur");
		CommonModConfig.starterBevel = configData.get("starter.bevel");
		CommonModConfig.starterSprocket = configData.get("starter.sprocket");
		CommonModConfig.starterCog = configData.get("starter.cog");
		CommonModConfig.starterCrown = configData.get("starter.crown");
		CommonModConfig.starterSun = configData.get("starter.sun");

		if (!configData.contains("value.spur")) configData.set("value.spur", 1);
		if (!configData.contains("value.bevel")) configData.set("value.bevel", 8);
		if (!configData.contains("value.sprocket")) configData.set("value.sprocket", 16);
		if (!configData.contains("value.cog")) configData.set("value.cog", 64);
		if (!configData.contains("value.crown")) configData.set("value.crown", 512);
		if (!configData.contains("value.sun")) configData.set("value.sun", 4096);

		Coin.SPUR.setValue(configData.get("value.spur"));
		Coin.BEVEL.setValue(configData.get("value.bevel"));
		Coin.SPROCKET.setValue(configData.get("value.sprocket"));
		Coin.COG.setValue(configData.get("value.cog"));
		Coin.CROWN.setValue(configData.get("value.crown"));
		Coin.SUN.setValue(configData.get("value.sun"));

		if (!configData.contains("general.currency")) configData.set("general.currency", "COG");
		CommonModConfig.currency  = Coin.valueOf(configData.get("general.currency"));
	}
}
