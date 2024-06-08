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

package dev.ithundxr.createnumismatics.base.data;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.ithundxr.createnumismatics.content.bank.BankTerminalBlock;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerBlock;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlock;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlock;
import net.minecraft.world.item.BlockItem;

public class BuilderTransformers {
    @ExpectPlatform
    public static <B extends AbstractDepositorBlock<?>, P> NonNullUnaryOperator<BlockBuilder<B, P>> depositor(String material) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <B extends BankTerminalBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> bankTerminal() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <B extends BlazeBankerBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> blazeBanker() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <B extends VendorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> vendor(boolean creative) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <I extends BlockItem, P> NonNullUnaryOperator<ItemBuilder<I, P>> vendorItem(boolean creative) {
        throw new AssertionError();
    }
}
