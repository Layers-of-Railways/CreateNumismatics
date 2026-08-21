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

import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.utility.Pointing;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListMenu;
import dev.ithundxr.createnumismatics.content.backend.trust_list.TrustListScreen;
import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorMenu;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorScreen;
import dev.ithundxr.createnumismatics.ponder.utils.SceneBuilderExtension;
import dev.ithundxr.createnumismatics.ponder.utils.ScreenVec;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement.Cursor;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement.CursorPhysicsProperties;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.function.Consumer;

public class BankingScenes {
    public static void trustList(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("trust_list", "Access Control with ID Cards");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos depositor = util.grid.at(1, 1, 1);
        Vec3 depositorText = util.vector.blockSurface(depositor, Direction.WEST);

        scene.world.showSection(util.select.position(depositor), Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(60)
            .attachKeyFrame()
            .text("Players with access can sneak-use most Numismatics blocks to configure their Trust List.")
            .pointAt(depositorText)
            .placeNearTarget();
        scene.idle(50);

        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(depositor), Pointing.DOWN)
            .rightClick()
            .whileSneaking(), 10);
        scene.idle(15);

        Consumer<Inventory> invFiller = inv -> {
            // Slimeist
            ItemStack id = NumismaticsItems.ID_CARDS.get(DyeColor.LIME).asStack();
            IDCardItem.set(id, new UUID(3911434881913931178L, -8657420175479844639L));
            inv.setItem(9, id);
        };

        var menu0 = scenex.showContainerMenu(
                5,
                depositor,
                NumismaticsBlockEntities.ANDESITE_DEPOSITOR.get(),
                (be, inv) -> new AndesiteDepositorMenu(NumismaticsMenuTypes.ANDESITE_DEPOSITOR.get(), -3, inv, be),
                AndesiteDepositorScreen::new,
                $ -> Component.translatable("block.numismatics.andesite_depositor")
            )
            .inventoryFiller(invFiller)
            .attachKeyFrame()
            .link();
        scenex.enableScreenOverlayLayer();
        scene.idle(15);

        scenex.modifyCursor(menu0, c -> c
            .setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOWER)
            .teleport(-20, -30)
            .setCursor(Cursor.NORMAL)
            .snapFrame());
        scene.idle(5);

        scenex.cursorTarget(ScreenVec.relative(menu0, 19 + 6, 23 + 6));
        scene.idle(15);

        scenex.hideContainerMenu(menu0, 5);

        var menu = scenex.showContainerMenu(
                5,
                depositor,
                NumismaticsBlockEntities.ANDESITE_DEPOSITOR.get(),
                (be, inv) -> new TrustListMenu(NumismaticsMenuTypes.TRUST_LIST.get(), -3, inv, be, NumismaticsBlocks.ANDESITE_DEPOSITOR.asStack()),
                TrustListScreen::new,
                $ -> Component.translatable("gui.numismatics.trust_list")
            )
            .inventoryFiller(invFiller)
            .attachKeyFrame()
            .link();

        scene.idle(5);

        scene.overlay.showText(80)
            .text("Players in the Trust List have full access to the shop, including the ability to modify the Trust List.")
            .independent(50);

        scenex.modifyCursor(menu, c -> c
            .setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOWER)
            .teleport(-20, -30)
            .snapFrame());
        scene.idle(5);

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));

        // pick up id card
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, TrustListMenu.PLAYER_INV_START_INDEX + 9));
        scene.idle(15);
        scenex.clickSlot(menu, TrustListMenu.PLAYER_INV_START_INDEX + 9);

        // place it
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, 2));
        scene.idle(15);
        scenex.clickSlot(menu, 2);
    }
}
