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
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.ponder.BlazeBankerScene;
import dev.ithundxr.createnumismatics.ponder.DepositorScenes;
import dev.ithundxr.createnumismatics.ponder.SalepointScenes;
import dev.ithundxr.createnumismatics.ponder.VendorScenes;

public class NumismaticsPonderIndex {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(Numismatics.MOD_ID);

    public static void register() {
        /* TODO
            See https://discord.com/channels/1226981107401232545/1261067581469757511/1495869753343082636
            Ponder Progress:
            - Depositors are basically done
            - Vendors need configuration explained (the current 'pricing' stub should probably be renamed 'configuration')
            - Salepoints need configuration explained
            - Blaze Banker's to-do is listed in its file
            - Bank Terminal maybe needs a coin-conversion-rate ponder
            - Bank Terminal/Authorized Card needs a subaccount ponder
            - Both types of payment Card need (shared) purchasing and fund-(source/target)-in-shops ponders
            - ID Cards need a trust list ponder
         */

        HELPER.forComponents(NumismaticsBlocks.ANDESITE_DEPOSITOR, NumismaticsBlocks.BRASS_DEPOSITOR)
            .addStoryBoard("depositors/intro", DepositorScenes::intro)
            .addStoryBoard("depositors/redstone", DepositorScenes::redstone)
            .addStoryBoard("depositors/pricing", DepositorScenes::pricing);

        HELPER.forComponents(NumismaticsBlocks.VENDOR, NumismaticsBlocks.CREATIVE_VENDOR)
            .addStoryBoard("vendors/intro", VendorScenes::intro)
            .addStoryBoard("vendors/config_sell", VendorScenes::configSell)
            .addStoryBoard("vendors/config_buy", VendorScenes::configBuy);

        HELPER.forComponents(NumismaticsBlocks.SALEPOINT)
            .addStoryBoard("salepoint", SalepointScenes::item);
    }

    // Any ponders that should appear AFTER creates own ponders should go here
    public static void registerAfterCreatePonders() {
        HELPER.forComponents(AllBlocks.BLAZE_BURNER)
            .addStoryBoard("blaze_banker", BlazeBankerScene::banker);
    }
}
