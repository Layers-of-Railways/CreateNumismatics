/*
 * Numismatics
 * Copyright (c) 2026 The Railways Team
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

package dev.ithundxr.createnumismatics.ponder;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import dev.ithundxr.createnumismatics.ponder.utils.SceneBuilderExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

// TODO finish usage ponder, add configuration ponder
public class VendorScenes {
    public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("vendor_intro", "Using Vendors");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos vendorSell = util.grid.at(1, 1, 1);
        BlockPos vendorBuy = util.grid.at(0, 1, 1);

        // for some reason the lighting is broken if we show it normally
        scene.world.showIndependentSection(util.select.position(vendorSell), Direction.DOWN);
        scene.idle(10);

        // fixme: tech demo, needs explanatory text. Should compare buy/sell mode and demo each, including sneak-for-bulk functionality
        scenex.showTextComponent(100)
            .text(vendorTooltip(vendorSell))
            .item(vendorTooltipItem(vendorSell))
            .preserveTextColor()
            .attachKeyFrame()
            .pointAt(vendorSell.getCenter())
            .placeNearTarget();
    }

    public static void pricing(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("vendor_pricing", "Vendor Pricing");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);
        scene.world.showSection(util.select.everywhere(), Direction.DOWN);
    }

    private static BiConsumer<PonderScene, List<Component>> vendorTooltip(BlockPos pos) {
        return (scene, tooltip) -> {
            if (scene.getWorld().getBlockEntity(pos) instanceof VendorBlockEntity vbe) {
                vbe.addToTooltip(tooltip, false);
            }
        };
    }

    private static Function<PonderScene, ItemStack> vendorTooltipItem(BlockPos pos) {
        return (scene) -> {
            if (scene.getWorld().getBlockEntity(pos) instanceof VendorBlockEntity vbe) {
                return vbe.getFilterItem();
            } else {
                return ItemStack.EMPTY;
            }
        };
    }
}
