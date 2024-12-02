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

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerBlockEntity;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerRenderer;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlockEntity;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlockEntity;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorRenderer;

public class NumismaticsBlockEntities {
    private static final CreateRegistrate REGISTRATE = Numismatics.registrate();

/*    public static final BlockEntityEntry<SemaphoreBlockEntity> SEMAPHORE = REGISTRATE.blockEntity("semaphore", SemaphoreBlockEntity::new)
        .validBlocks(CRBlocks.SEMAPHORE)
        .renderer(() -> SemaphoreRenderer::new)
        .register();*/

    public static final BlockEntityEntry<AndesiteDepositorBlockEntity> ANDESITE_DEPOSITOR = REGISTRATE.blockEntity("andesite_depositor", AndesiteDepositorBlockEntity::new)
        .validBlocks(NumismaticsBlocks.ANDESITE_DEPOSITOR)
        .register();

    public static final BlockEntityEntry<BrassDepositorBlockEntity> BRASS_DEPOSITOR = REGISTRATE.blockEntity("brass_depositor", BrassDepositorBlockEntity::new)
        .validBlocks(NumismaticsBlocks.BRASS_DEPOSITOR)
        .register();

    public static final BlockEntityEntry<BlazeBankerBlockEntity> BLAZE_BANKER = REGISTRATE.blockEntity("blaze_banker", BlazeBankerBlockEntity::new)
        .validBlocks(NumismaticsBlocks.BLAZE_BANKER)
        .renderer(() -> BlazeBankerRenderer::new)
        .register();

    public static final BlockEntityEntry<VendorBlockEntity> VENDOR = REGISTRATE.blockEntity("vendor", VendorBlockEntity::new)
        .validBlocks(NumismaticsBlocks.VENDOR, NumismaticsBlocks.CREATIVE_VENDOR)
        .renderer(() -> VendorRenderer::new)
        .register();

    public static final BlockEntityEntry<SalepointBlockEntity> SALEPOINT = REGISTRATE.blockEntity("salepoint", SalepointBlockEntity::new)
        .validBlocks(NumismaticsBlocks.SALEPOINT)
        .register();

    @SuppressWarnings("EmptyMethod")
    public static void register() {}
}
