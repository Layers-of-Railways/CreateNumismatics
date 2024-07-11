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
import dev.ithundxr.createnumismatics.ponder.DepositorScene;

public class NumismaticsPonderIndex {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper(Numismatics.MOD_ID);

    public static void register() {
        HELPER.forComponents(NumismaticsBlocks.ANDESITE_DEPOSITOR, NumismaticsBlocks.BRASS_DEPOSITOR)
                .addStoryBoard("depositor", DepositorScene::depositor);
    }

    // Any ponders that should appear AFTER creates own ponders should go here
    public static void registerAfterCreatePonders() {
        HELPER.forComponents(AllBlocks.BLAZE_BURNER)
                .addStoryBoard("blaze_banker", BlazeBankerScene::banker);
    }
}
