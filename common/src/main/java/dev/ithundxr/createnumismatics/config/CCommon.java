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

package dev.ithundxr.createnumismatics.config;

import com.simibubi.create.foundation.config.ConfigBase;
import dev.ithundxr.createnumismatics.content.backend.Coin;

@SuppressWarnings("unused")
public class CCommon extends ConfigBase {

    public final ConfigGroup coins = group(0, "coins", Comments.coins);

    public final ConfigEnum<Coin> defaultCoin = e(Coin.SPUR, "defaultCoin", Comments.defaultCoin);
    
    // Based off of https://github.com/Layers-of-Railways/Railway/blob/68713f0fbb20080b7e207c070b1595bdbbc1bc00/common/src/main/java/com/railwayteam/railways/config/CCommon.java
    
    @Override
    public String getName() {
        return "common";
    }

    private static class Comments {
        static final String coins = "Coin settings";

        static final String defaultCoin = "The default coin to be used in UI related displays";
    }
}
