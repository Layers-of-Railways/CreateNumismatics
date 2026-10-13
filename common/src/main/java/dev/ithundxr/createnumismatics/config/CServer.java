/*
 * Numismatics
 * Copyright (c) 2024-2026 The Railways Team
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

import net.createmod.catnip.config.ConfigBase;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class CServer extends ConfigBase {

    public final ConfigGroup coins = group(0, "coins", Comments.coins);

    public final ConfigGroup starterCurrency = group(1, "starterCurrency", Comments.starterCurrency);
    
    public final ConfigInt starterSpurs = i(0, 0, "starter_spurs");
    public final ConfigInt starterBevels = i(0, 0, "starter_bevels");
    public final ConfigInt starterSprockets = i(0, 0, "starter_sprockets");
    public final ConfigInt starterCogs = i(0, 0, "starter_cogs");
    public final ConfigInt starterCrowns = i(0, 0, "starter_crowns");
    public final ConfigInt starterSuns = i(0, 0, "starter_suns");

    public final ConfigGroup loginRewards = group(2, "loginRewards", Comments.loginRewards);

    public final ConfigBool enableDailyRewards = b(false, "enableDailyRewards", Comments.enableDailyRewards);
    public final ConfigInt dailySpurs = i(0, 0, "daily_spurs");
    public final ConfigInt dailyBevels = i(0, 0, "daily_bevels");
    public final ConfigInt dailySprockets = i(0, 0, "daily_sprockets");
    public final ConfigInt dailyCogs = i(0, 0, "daily_cogs");
    public final ConfigInt dailyCrowns = i(0, 0, "daily_crowns");
    public final ConfigInt dailySuns = i(0, 0, "daily_suns");

    public final ConfigBool enableWeeklyRewards = b(false, "enableWeeklyRewards", Comments.enableWeeklyRewards);
    public final ConfigInt weeklySpurs = i(0, 0, "weekly_spurs");
    public final ConfigInt weeklyBevels = i(0, 0, "weekly_bevels");
    public final ConfigInt weeklySprockets = i(0, 0, "weekly_sprockets");
    public final ConfigInt weeklyCogs = i(0, 0, "weekly_cogs");
    public final ConfigInt weeklyCrowns = i(0, 0, "weekly_crowns");
    public final ConfigInt weeklySuns = i(0, 0, "weekly_suns");

    public final ConfigBool notifyOnLoginReward = b(true, "notifyOnReward", Comments.notifyOnLoginReward);

    public final ConfigGroup computerCraft = group(0, "computerCraft", Comments.computerCraft);

    public final ConfigBool getSubAccountsCommand = b(false, "get_sub_accounts_command", Comments.getSubAccountsCommand);
    
    
    //public final ConfigGroup misc = group(0, "misc", Comments.misc);

    // Based off of https://github.com/Layers-of-Railways/Railway/blob/68713f0fbb20080b7e207c070b1595bdbbc1bc00/common/src/main/java/com/railwayteam/railways/config/CServer.java

    @Override
    public @NotNull String getName() {
        return "server";
    }

    private static class Comments {
        static final String coins = "Coin settings";

        static final String starterCurrency = "How much of this coin type should players receive in their bank account on first join";;

        static final String loginRewards = "Daily and weekly login rewards deposited to the player's bank account. Boundaries use UTC calendar days and ISO weeks starting Monday";

        static final String enableDailyRewards = "Grant the daily reward once per UTC calendar day on login";
        static final String enableWeeklyRewards = "Grant the weekly reward once per ISO week (Monday 00:00 UTC) on login";
        static final String notifyOnLoginReward = "Send a chat message when a login reward is granted";

        static final String computerCraft = "Settings relating to ComputerCraft compatibility";

        static final String getSubAccountsCommand = "Enables the getSubAccounts function. Disabled by default due to security concerns.";

        static final String misc = "Miscellaneous settings";
    }
}