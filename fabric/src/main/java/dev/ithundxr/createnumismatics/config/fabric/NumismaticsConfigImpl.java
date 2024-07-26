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

package dev.ithundxr.createnumismatics.config.fabric;

import com.simibubi.create.foundation.config.ConfigBase;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.config.NumismaticsConfig;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Map;

public class NumismaticsConfigImpl {
    public static void register() {
        NumismaticsConfig.registerCommon();

        for (Map.Entry<ModConfig.Type, ConfigBase> pair : NumismaticsConfig.CONFIGS.entrySet())
            ForgeConfigRegistry.INSTANCE.register(Numismatics.MOD_ID, pair.getKey(), pair.getValue().specification);

        ModConfigEvents.loading(Numismatics.MOD_ID).register(NumismaticsConfig::onLoad);
        ModConfigEvents.reloading(Numismatics.MOD_ID).register(NumismaticsConfig::onReload);
    }
}
