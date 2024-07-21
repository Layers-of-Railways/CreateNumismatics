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
public class CServer extends ConfigBase {

    public final ConfigGroup coins = group(0, "coins", Comments.coins);

    public final ConfigGroup starter_currency = group(1, "starter_currency", Comments.starterMoney);
    
    public final ConfigInt starter_spurs = i(0, 0, "starter_spurs");
    public final ConfigInt starter_bevels = i(0, 0, "starter_bevels");
    public final ConfigInt starter_sprockets = i(0, 0, "starter_sprockets");
    public final ConfigInt starter_cogs = i(0, 0, "starter_cogs");
    public final ConfigInt starter_crowns = i(0, 0, "starter_crowns");
    public final ConfigInt starter_suns = i(0, 0, "starter_suns");
    
    
    //public final ConfigGroup misc = group(0, "misc", Comments.misc);

    // Based off of https://github.com/Layers-of-Railways/Railway/blob/68713f0fbb20080b7e207c070b1595bdbbc1bc00/common/src/main/java/com/railwayteam/railways/config/CServer.java

    @Override
    public String getName() {
        return "server";
    }

    private static class Comments {
        static final String coins = "Coin settings";
        
        static final String starterMoney = "How much of this coin type should players receive in their bank account on first join";;
        
        static final String misc = "Miscellaneous settings";
    }
}