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

package dev.ithundxr.createnumismatics.content.backend;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.config.CServer;
import dev.ithundxr.createnumismatics.config.NumismaticsConfig;
import dev.ithundxr.createnumismatics.content.backend.BankAccount.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoginRewardManager {
    public static void handleLogin(ServerPlayer player) {
        handleLogin(player, LocalDate.now(ZoneOffset.UTC));
    }

    static void handleLogin(ServerPlayer player, LocalDate today) {
        CServer config = NumismaticsConfig.server();
        BankAccount account = Numismatics.BANK.getOrCreateAccount(player.getUUID(), Type.PLAYER);

        long todayEpoch = today.toEpochDay();
        long currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay();
        long lastMonday = mondayOf(account.getLastWeeklyClaimYear(), account.getLastWeeklyClaimWeek());

        if (config.enableDailyRewards.get() && todayEpoch > account.getLastDailyClaimDay()) {
            Map<Coin, Integer> daily = dailyReward(config);
            account.setLastDailyClaimDay(todayEpoch);
            grant(account, player, daily, "gui.numismatics.login_reward.daily", config);
        }

        if (config.enableWeeklyRewards.get() && (lastMonday < 0 || currentMonday > lastMonday)) {
            Map<Coin, Integer> weekly = weeklyReward(config);
            account.setLastWeeklyClaim(today.get(IsoFields.WEEK_BASED_YEAR), today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            grant(account, player, weekly, "gui.numismatics.login_reward.weekly", config);
        }
    }

    static long mondayOf(int weekYear, int week) {
        if (weekYear < 0 || week < 0)
            return -1;
        return LocalDate.of(weekYear, 1, 4)
            .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toEpochDay();
    }

    static Map<Coin, Integer> dailyReward(CServer config) {
        Map<Coin, Integer> reward = new LinkedHashMap<>();
        reward.put(Coin.SPUR, config.dailySpurs.get());
        reward.put(Coin.BEVEL, config.dailyBevels.get());
        reward.put(Coin.SPROCKET, config.dailySprockets.get());
        reward.put(Coin.COG, config.dailyCogs.get());
        reward.put(Coin.CROWN, config.dailyCrowns.get());
        reward.put(Coin.SUN, config.dailySuns.get());
        return reward;
    }

    static Map<Coin, Integer> weeklyReward(CServer config) {
        Map<Coin, Integer> reward = new LinkedHashMap<>();
        reward.put(Coin.SPUR, config.weeklySpurs.get());
        reward.put(Coin.BEVEL, config.weeklyBevels.get());
        reward.put(Coin.SPROCKET, config.weeklySprockets.get());
        reward.put(Coin.COG, config.weeklyCogs.get());
        reward.put(Coin.CROWN, config.weeklyCrowns.get());
        reward.put(Coin.SUN, config.weeklySuns.get());
        return reward;
    }

    private static void grant(BankAccount account, ServerPlayer player, Map<Coin, Integer> reward, String messageKey, CServer config) {
        int total = 0;
        for (Map.Entry<Coin, Integer> entry : reward.entrySet()) {
            int amount = entry.getValue();
            if (amount <= 0)
                continue;
            account.deposit(entry.getKey(), amount);
            total += amount;
        }
        if (total <= 0)
            return;
        if (config.notifyOnLoginReward.get())
            player.displayClientMessage(Component.translatable(messageKey, rewardSummary(reward)), false);
    }

    static Component rewardSummary(Map<Coin, Integer> reward) {
        List<Component> parts = new ArrayList<>();
        for (Map.Entry<Coin, Integer> entry : reward.entrySet()) {
            if (entry.getValue() <= 0)
                continue;
            String key = entry.getKey().getTranslationKey() + (entry.getValue() == 1 ? "" : ".plural");
            parts.add(Component.literal(entry.getValue() + " ").append(Component.translatable(key)));
        }
        MutableComponent result = Component.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0)
                result.append(Component.literal(", "));
            result.append(parts.get(i));
        }
        return result;
    }
}
