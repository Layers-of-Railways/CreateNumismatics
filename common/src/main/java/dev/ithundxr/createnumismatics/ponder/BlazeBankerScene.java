/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
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
import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerMenu;
import dev.ithundxr.createnumismatics.content.bank.blaze_banker.BlazeBankerScreen;
import dev.ithundxr.createnumismatics.ponder.utils.SceneBuilderExtension;
import dev.ithundxr.createnumismatics.ponder.utils.ScreenVec;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement.Cursor;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class BlazeBankerScene {
    public static void banker(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("blaze_banker", "Banking with Blazes");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos blazeBurner = util.grid.at(2, 1, 2);
        Vec3 blazeBurnerText = util.vector.blockSurface(blazeBurner, Direction.WEST);

        scene.world.showSection(util.select.position(blazeBurner), Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("Create a Blaze Banker by applying a Banking Guide to a Blaze Burner")
                .pointAt(blazeBurnerText)
                .placeNearTarget();
        scene.idle(80);

        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(blazeBurner), Pointing.DOWN).leftClick()
                .withItem(NumismaticsItems.BANKING_GUIDE.asStack()), 15);
        scene.idle(7);
        scene.world.setBlock(blazeBurner, NumismaticsBlocks.BLAZE_BANKER.getDefaultState(), false);
        scene.idle(20);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("The Blaze Banker is a way for players to manage money without using their own account.")
                .pointAt(blazeBurnerText)
                .placeNearTarget();
        scene.idle(80);

        scene.overlay.showText(60)
            .attachKeyFrame()
            .text("Players with access can use Blaze Bankers to configure them.")
            .pointAt(blazeBurnerText)
            .placeNearTarget();
        scene.idle(50);

        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(blazeBurner), Pointing.DOWN)
            .rightClick(), 10);
        scene.idle(15);

        var menu = scenex.showContainerMenu(
                5,
                blazeBurner,
                NumismaticsBlockEntities.BLAZE_BANKER.get(),
                (be, inv) -> new BlazeBankerMenu(NumismaticsMenuTypes.BLAZE_BANKER.get(), -3, inv, be),
                BlazeBankerScreen::new,
                $ -> Component.translatable("block.numismatics.blaze_banker")
            )
            .inventoryFiller(inv -> {
                inv.setItem(9, NumismaticsItems.CARDS.get(DyeColor.RED).asStack());

                // Slimeist
                ItemStack id = NumismaticsItems.ID_CARDS.get(DyeColor.LIME).asStack();
                IDCardItem.set(id, new UUID(3911434881913931178L, -8657420175479844639L));
                inv.setItem(10, id);
            })
            .attachKeyFrame()
            .link();
        scenex.enableScreenOverlayLayer();
        scene.idle(15);

        scenex.modifyCursor(menu, c -> c
            .setPhysics(VirtualScreenElement.CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW)
            .teleport(-20, -30)
            .snapFrame());

        scenex.showText(50, ScreenVec.slotRelative(menu, 20, 7, BlazeBankerMenu.CARD_SLOT_INDEX))
            .text("To bind a card to a Banker, place it into its card slot.")
            .placeNearTarget();
        scene.idle(5);

        scenex.modifyCursor(menu, c -> c.setCursor(Cursor.NORMAL));
        scenex.cursorTarget(ScreenVec.relative(menu, -20, 60));
        scene.idle(2);
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, BlazeBankerMenu.PLAYER_INV_START_INDEX + 9));
        scene.idle(18);

        // pick up bank card
        scenex.clickSlot(menu, BlazeBankerMenu.PLAYER_INV_START_INDEX + 9);
        scenex.modifyCursor(menu, $ -> $.setPhysics(VirtualScreenElement.CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOWER));
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, BlazeBankerMenu.CARD_SLOT_INDEX));
        scene.idle(15);

        // bind card
        scenex.clickSlot(menu, BlazeBankerMenu.CARD_SLOT_INDEX);
        scene.idle(30);

        // pick up bank card to put back in inv
        scenex.clickSlot(menu, BlazeBankerMenu.CARD_SLOT_INDEX);
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, BlazeBankerMenu.PLAYER_INV_START_INDEX + 9));
        scene.idle(15);
        scenex.clickSlot(menu, BlazeBankerMenu.PLAYER_INV_START_INDEX + 9);
        scene.idle(10);

        scenex.showText(120, ScreenVec.slotRelative(menu, 20, 1, 8))
            .text("Unlike with Sub Accounts, players trusted directly in a Blaze Banker have full access, including to a Bank Terminal when using a bound Card.")
            .placeNearTarget()
            .attachKeyFrame();
        scene.idle(5);

        // pick up id card to put in trust list
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 10, 10, BlazeBankerMenu.PLAYER_INV_START_INDEX + 10));
        scene.idle(15);
        scenex.clickSlot(menu, BlazeBankerMenu.PLAYER_INV_START_INDEX + 10);

        // move and put in trust list
        scenex.cursorTarget(ScreenVec.slotRelative(menu, 7, 7, 0));
        scene.idle(15);
        scenex.clickSlot(menu, 0);
    }
}
