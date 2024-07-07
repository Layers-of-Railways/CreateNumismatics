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
	}
}
