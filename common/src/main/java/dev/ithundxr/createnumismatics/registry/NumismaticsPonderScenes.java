/*
 * Numismatics
 * Copyright (c) 2023-2026 The Railways Team
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
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.ithundxr.createnumismatics.ponder.BankingScenes;
import dev.ithundxr.createnumismatics.ponder.BlazeBankerScene;
import dev.ithundxr.createnumismatics.ponder.DepositorScenes;
import dev.ithundxr.createnumismatics.ponder.SalepointScenes;
import dev.ithundxr.createnumismatics.ponder.VendorScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

// TODO: fix up imports

public class NumismaticsPonderScenes {
    private static @Nullable PonderSceneRegistrationHelper<ResourceLocation> initialHelper;
    private static boolean createPondersRegistered = false;

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        if (!createPondersRegistered)
            initialHelper = helper;
        /* TODO
            See https://discord.com/channels/1226981107401232545/1261067581469757511/1495869753343082636
            Ponder Progress:
            - Depositors are basically done
            - Vendors are basically done
            - Salepoints are basically done
            - Blaze Bankers are basically done
            - Bank Terminal maybe needs a coin-conversion-rate ponder
            - Bank Terminal/Authorized Card needs a subaccount ponder
            - Both types of payment Card need (shared) purchasing and fund-(source/target)-in-shops ponders
            - ID Cards have a trust list ponder
         */

        HELPER.forComponents(NumismaticsBlocks.ANDESITE_DEPOSITOR, NumismaticsBlocks.BRASS_DEPOSITOR)
            .addStoryBoard("depositors/intro", DepositorScenes::intro, NumismaticsPonderTags.SHOPS)
            .addStoryBoard("depositors/redstone", DepositorScenes::redstone)
            .addStoryBoard("depositors/pricing", DepositorScenes::pricing)
            .addStoryBoard("depositors/automated_storage", DepositorScenes::automatedStorage);

        HELPER.forComponents(NumismaticsBlocks.VENDOR, NumismaticsBlocks.CREATIVE_VENDOR)
            .addStoryBoard("vendors/intro", VendorScenes::intro, NumismaticsPonderTags.SHOPS)
            .addStoryBoard("vendors/config_sell", VendorScenes::configSell)
            .addStoryBoard("vendors/config_buy", VendorScenes::configBuy)
            .addStoryBoard("vendors/config_emi", VendorScenes::configEmi);

        HELPER.forComponents(NumismaticsBlocks.SALEPOINT)
            .addStoryBoard("salepoint", SalepointScenes::item, NumismaticsPonderTags.SHOPS);

        HELPER.forComponents(iterableThenVarArgs(
                NumismaticsItems.ID_CARDS,
                NumismaticsBlocks.ANDESITE_DEPOSITOR, NumismaticsBlocks.BRASS_DEPOSITOR,
                NumismaticsBlocks.VENDOR, NumismaticsBlocks.CREATIVE_VENDOR,
                NumismaticsBlocks.SALEPOINT
            ))
            .addStoryBoard("trust_list", BankingScenes::trustList);

        if (createPondersRegistered) {
            HELPER.forComponents(AllBlocks.BLAZE_BURNER, NumismaticsItems.BANKING_GUIDE)
                .addStoryBoard("blaze_banker", BlazeBankerScene::banker);
        }
    }

    // Any ponders that should appear AFTER creates own ponders should go here
    public static void registerAfterCreatePonders() {
        createPondersRegistered = true;
        if (initialHelper == null)
            return;
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = initialHelper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(AllBlocks.BLAZE_BURNER, NumismaticsItems.BANKING_GUIDE)
            .addStoryBoard("blaze_banker", BlazeBankerScene::banker);
    }

    @SuppressWarnings("SameParameterValue")
    private static Iterable<? extends ItemProviderEntry<?>> iterableThenVarArgs(
        Iterable<? extends ItemProviderEntry<?>> iter,
        ItemProviderEntry<?>... items
    ) {
        if (items.length == 0)
            return iter;

        return () -> new Iterator<>() {
            private int i = -1;
            private final Iterator<? extends ItemProviderEntry<?>> iter$ = iter.iterator();

            @Override
            public boolean hasNext() {
                return i < items.length;
            }

            @Override
            public ItemProviderEntry<?> next() {
                if (i == -1) {
                    if (iter$.hasNext())
                        return iter$.next();
                    else
                        i = 0;
                }
                return items[i++];
            }
        };
    }
}
