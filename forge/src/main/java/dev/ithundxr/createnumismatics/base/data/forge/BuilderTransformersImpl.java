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

package dev.ithundxr.createnumismatics.base.data.forge;

import com.simibubi.create.Create;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.bank.BankTerminalBlock;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerBlock;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlock;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.client.model.generators.ConfiguredModel;

public class BuilderTransformersImpl {
    public static <B extends AbstractDepositorBlock<?>, P> NonNullUnaryOperator<BlockBuilder<B, P>> depositor(String material) {
        return a -> a.blockstate((c, p) -> p.getVariantBuilder(c.get())
            .forAllStatesExcept((state) -> ConfiguredModel.builder()
                    .modelFile(p.models()
                        .orientable(c.getName() + (state.getValue(AbstractDepositorBlock.LOCKED) ? "_locked" : ""),
                            Create.asResource("block/" + material + "_casing"),
                            p.modLoc("block/depositor/" + material + "_depositor_slot" + (state.getValue(AbstractDepositorBlock.LOCKED) ? "_locked" : "")),
                            p.modLoc("block/depositor/" + material + "_depositor_select"))
                        .texture("particle", Create.asResource("block/" + material + "_casing"))
                    )
                    .rotationY((int) state.getValue(AbstractDepositorBlock.HORIZONTAL_FACING).toYRot() + 180)
                    .build(),
                AbstractDepositorBlock.POWERED
            )
        );
    }

    public static <B extends BankTerminalBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> bankTerminal() {
        return a -> a.blockstate((c, p) -> p.getVariantBuilder(c.get())
            .forAllStates((state) -> ConfiguredModel.builder()
                .modelFile(p.models()
                    .getExistingFile(p.modLoc("block/bank_terminal"))
                )
                .rotationY((int) state.getValue(BankTerminalBlock.HORIZONTAL_FACING).toYRot() + 180)
                .build()
            )
        );
    }

    public static <B extends BlazeBankerBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> blazeBanker() {
        return a -> a.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
                .getExistingFile(Create.asResource("block/blaze_burner/block"))
            )
        );
    }

    public static <B extends VendorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> vendor(boolean creative) {
        return a -> a
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), p.models()
                .getExistingFile(Numismatics.asResource("block/"+(creative?"creative_":"")+"display_case"))
            ));
    }

    public static <I extends BlockItem, P> NonNullUnaryOperator<ItemBuilder<I, P>> vendorItem(boolean creative) {
        return a -> a
            .model((c, p) -> p.withExistingParent(
                c.getName(),
                Numismatics.asResource("block/"+(creative?"creative_":"")+"display_case")
            ));
    }
}
