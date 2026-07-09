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

import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pointing;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorMenu;
import dev.ithundxr.createnumismatics.content.vendor.VendorScreen;
import dev.ithundxr.createnumismatics.mixin_interfaces.InputWindowElement_Duck;
import dev.ithundxr.createnumismatics.ponder.utils.SceneBuilderExtension;
import dev.ithundxr.createnumismatics.ponder.utils.ScreenVec;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement.Cursor;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

// TODO add configuration ponder
@SuppressWarnings("DuplicatedCode")
public class VendorScenes {
    public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("vendor_intro", "Using Vendors");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos vendorSell = util.grid.at(1, 1, 1);
        BlockPos vendorBuy = util.grid.at(0, 1, 1);
        Vec3 vendorText = util.vector.blockSurface(vendorSell, Direction.WEST).add(0, 0.5, 0);

        // for some reason the lighting is broken if we show it normally
        var vendorSellLink = scene.world.showIndependentSection(util.select.position(vendorSell), Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(70)
            .text("This vendor is selling 8 apples for a cog and a bevel.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(80);

        scenex.showTextComponent(100)
            .text(vendorTooltip(vendorSell))
            .item(vendorTooltipItem(vendorSell))
            .preserveTextColor()
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(110);

        scene.overlay.showText(130)
            .text("Use the vendor to buy 8 apples, sneak-use it to buy a whole stack.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();

        scene.idle(20);

        for (boolean bulk : Iterate.falseAndTrue) {
            tradeInteraction(
                scene,
                util,
                vendorSell,
                (bulk ? Coin.COG : Coin.BEVEL).asStack(9),
                new ItemStack(Items.GOLDEN_APPLE, bulk ? 64 : 8),
                bulk
            );
        }

        scene.idle(20);

        scene.world.hideIndependentSection(vendorSellLink, Direction.EAST);
        scene.idle(5);
        var vendorBuyLink = scene.world.showIndependentSection(util.select.position(vendorBuy), Direction.EAST);
        scene.world.moveSection(vendorBuyLink, Vec3.atLowerCornerOf(vendorSell.subtract(vendorBuy)), 20);
        scene.idle(30);

        scene.overlay.showText(70)
            .text("This vendor is buying 16 oak logs for a bevel.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(80);

        scenex.showTextComponent(100)
            .text(vendorTooltip(vendorBuy))
            .item(vendorTooltipItem(vendorBuy))
            .preserveTextColor()
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();
        scene.idle(110);

        scene.overlay.showText(130)
            .text("Use the vendor to sell 16 oak logs, sneak-use it to sell a whole stack.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();

        scene.idle(20);

        for (boolean bulk : Iterate.falseAndTrue) {
            tradeInteraction(
                scene,
                util,
                vendorSell,
                new ItemStack(Items.OAK_LOG, bulk ? 64 : 16),
                Coin.BEVEL.asStack(bulk ? 4 : 1),
                bulk
            );
        }
    }

    public static void config(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("vendor_config", "Vendor Configuration");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos vendorSell = util.grid.at(1, 1, 1);
        BlockPos vendorBuy = util.grid.at(0, 1, 1);
        Vec3 vendorText = util.vector.blockSurface(vendorSell, Direction.WEST).add(0, 0.5, 0);

        // for some reason the lighting is broken if we show it normally
        var vendorSellLink = scene.world.showIndependentSection(util.select.position(vendorSell), Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(60)
            .text("Players with access to a vendor can sneak-use to configure it.")
            .attachKeyFrame()
            .pointAt(vendorText)
            .placeNearTarget();

        scene.idle(50);

        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(vendorSell), Pointing.DOWN)
            .rightClick()
            .whileSneaking(),
            10);
        scene.idle(15);

        var menu = scenex.showContainerMenu(
                5,
                vendorSell,
                NumismaticsBlockEntities.VENDOR.get(),
                (be, inv) -> new VendorMenu(NumismaticsMenuTypes.VENDOR.get(), -3, inv, be),
                VendorScreen::new
            )
            .colored(PonderPalette.RED)
            .attachKeyFrame()
            .link();

        scenex.enableScreenOverlayLayer();
        scenex.modifyCursor(menu, c -> c
            .setCursor(Cursor.HIDDEN)
            .teleport(-20, -30)
            .snapFrame());

        scene.idle(15);

        scene.idle(20);

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scenex.cursorTarget(ScreenVec.relative(menu, 120, 84));

        scene.idle(50);

        scenex.cursorTarget(ScreenVec.relative(menu, 70, 100));

        scene.idle(10);

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.SCROLL_UP));

        for (int i = 0; i < 59; i ++) {
            scenex.showText(60, ScreenVec.slotRelative(menu, 0, 0, i))
                .text(""+i)
                .colored(PonderPalette.values()[i % PonderPalette.values().length])
                .placeNearTarget();
            scene.idle(5);
        }

        scene.idle(80);

        scenex.disableScreenOverlayLayer();

        scenex.hideContainerMenu(menu, 5);
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

    private static void tradeInteraction(SceneBuilder scene, SceneBuildingUtil util, BlockPos vendorPos,
                                         ItemStack intoVendor, ItemStack fromVendor, boolean bulk) {
        InputWindowElement iwe = new InputWindowElement(util.vector.topOf(vendorPos), Pointing.DOWN)
            .withItem(intoVendor)
            .rightClick();
        if (bulk) iwe.whileSneaking();
        ((InputWindowElement_Duck) iwe).numismatics$showItemCount(true);
        scene.overlay.showControls(iwe, 40);
        scene.idle(6);

        scene.effects.indicateSuccess(vendorPos);
        var item = scene.world.createItemEntity(
            util.vector.blockSurface(vendorPos, Direction.NORTH),
            new Vec3(0, 0.15, -0.15),
            fromVendor
        );
        scene.idle(36);
        scene.idle(10);

        scene.world.modifyEntity(item, Entity::discard);
    }
}
