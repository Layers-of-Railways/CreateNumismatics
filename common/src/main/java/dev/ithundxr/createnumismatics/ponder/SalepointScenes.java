/*
 * Numismatics
 * Copyright (c) 2024-2026 The Railways Team
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

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.PonderWorld;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instruction.PonderInstruction;
import com.simibubi.create.foundation.utility.Pointing;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointBlockEntity;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointConfigMenu;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointConfigScreen;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointPurchaseMenu;
import dev.ithundxr.createnumismatics.content.salepoint.SalepointPurchaseScreen;
import dev.ithundxr.createnumismatics.content.salepoint.states.ItemSalepointState;
import dev.ithundxr.createnumismatics.mixin_interfaces.InputWindowElement_Duck;
import dev.ithundxr.createnumismatics.mixin_interfaces.StandardBogeyBlockEntity_Duck;
import dev.ithundxr.createnumismatics.ponder.utils.SceneBuilderExtension;
import dev.ithundxr.createnumismatics.ponder.utils.ScreenVec;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement.Cursor;
import dev.ithundxr.createnumismatics.ponder.utils.elements.VirtualScreenElement.CursorPhysicsProperties;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import dev.ithundxr.createnumismatics.registry.NumismaticsMenuTypes;
import dev.ithundxr.createnumismatics.registry.NumismaticsShapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SalepointScenes {
    public static void item(SceneBuilder scene, SceneBuildingUtil util) {
        SceneBuilderExtension scenex = new SceneBuilderExtension(scene);
        scene.title("salepoint_item", "Using Salepoints");
        scene.configureBasePlate(1, 0, 12);
        scene.scaleSceneView(.60f);
        scene.setSceneOffsetY(-1);
        scene.showBasePlate();
        scene.idle(10);

        ItemStack ironIngotStack = new ItemStack(Items.IRON_INGOT, 64);
        ItemStack brassIngotStack = AllItems.BRASS_INGOT.asStack(64);

        BlockPos salepointPos = util.grid.at(11, 3, 4);
        Vec3 salepointTextVec = util.vector.blockSurface(salepointPos, Direction.WEST, -11 / 16f);
        Selection salepoint = util.select.position(salepointPos);
        Selection salepointSupport = util.select.fromTo(12, 1, 4, 12, 3, 4);
        BlockPos stationaryPSIPos = util.grid.at(7, 2, 4);
        Selection stationaryPSI = util.select.position(stationaryPSIPos);
        BlockPos funnelPos = util.grid.at(7, 2, 3);
        Selection funnel = util.select.position(funnelPos);
        Selection station = util.select.position(11, 1, 9);

        Selection beltIntoFunnel = util.select.fromTo(7, 1, 2, 7, 1, 3);
        Selection beltIntoBelt = util.select.fromTo(12, 1, 2, 8, 1, 2);

        BlockPos beltStart = util.grid.at(12, 1, 2);

        Selection gearBoxes = util.select.fromTo(8, 1, 3, 9, 1, 3);
        Selection smallCog = util.select.position(12, 1, 3);
        Selection largeCog = util.select.position(13, 0, 3);

        Selection fluidLargeCog = util.select.position(13, 4, 4);
        Selection fluidSmallCogAndShaft = util.select.fromTo(13, 5, 3, 10, 5, 3);
        Selection fluidPipe = mergedSelection(
            util.select.fromTo(12, 5, 2, 7, 5, 2),
            util.select.fromTo(7, 6, 2, 7, 6, 4)
        );

        BlockPos bogey1 = util.grid.at(10, 2, 6);
        BlockPos bogey2 = util.grid.at(6, 2, 6);

        Selection train1 = util.select.fromTo(12, 2, 5, 8, 3, 7);
        Selection train2 = util.select.fromTo(7, 2, 5, 4, 3, 7);
        Selection train2a = util.select.fromTo(7, 2, 7, 4, 3, 10);

        Selection assembledPSI = util.select.position(7, 2, 6);
        Selection bothPSI = stationaryPSI.add(assembledPSI);
        Class<PortableItemInterfaceBlockEntity> classPSI = PortableItemInterfaceBlockEntity.class;

        BlockPos assembledBarrel = util.grid.at(6, 3, 6);

        // Show tracks
        for (int i = 13; i >= 0; i--) {
            scene.world.showSection(util.select.position(i, 1, 6), Direction.DOWN);
            scene.idle(1);
        }

        // Show item sale mechanisms
        Selection[] toReveal = new Selection[] {
            largeCog,
            smallCog,
            beltIntoBelt,
            gearBoxes,
            beltIntoFunnel,
            stationaryPSI,
            funnel,
            station
        };

        for (Selection sel : toReveal) {
            scene.world.showSection(sel, Direction.DOWN);
            scene.idle(5);
        }

        // Spawn iron
        for (int i = 0; i < 6; i++) {
            ElementLink<EntityElement> item = scene.world.createItemEntity(
                util.vector.centerOf(beltStart.above(3)),
                util.vector.of(0, 0, 0),
                ironIngotStack
            );
            scene.idle(13);
            scene.world.modifyEntity(item, Entity::discard);

            scene.world.createItemOnBelt(beltStart, Direction.DOWN, ironIngotStack);
            scene.idle(5);

            if (i == 3) {
                scene.overlay.showText(70)
                    .attachKeyFrame()
                    .text("Normally, stationary Interfaces will automatically transfer items to any assembled Interfaces that are attached.")
                    .pointAt(stationaryPSI.getCenter())
                    .placeNearTarget();
            }
        }

        // Reveal first item train
        coupleTrain(scene, bogey1, 4, Direction.NORTH);

        ElementLink<WorldSectionElement> trainElement1 = scene.world.showIndependentSection(train1, Direction.DOWN);
        scene.world.moveSection(trainElement1, util.vector.of(-14, 0, 0), 0);

        ElementLink<WorldSectionElement> trainElement2 = scene.world.showIndependentSection(train2, Direction.DOWN);
        scene.world.moveSection(trainElement2, util.vector.of(-14, 0, 0), 0);

        scene.world.moveSection(trainElement1, util.vector.of(14, 0, 0), 50);
        scene.world.animateBogey(bogey1, -14, 50);
        scene.world.moveSection(trainElement2, util.vector.of(14, 0, 0), 50);
        scene.world.animateBogey(bogey2, -14, 50);

        scene.idle(50);

        scene.world.modifyBlockEntityNBT(bothPSI, classPSI, nbt -> {
            nbt.putFloat("Distance", 1);
            nbt.putFloat("Timer", 12);
        });

        scene.idle(5);

        // fill first item train
        for (int i = 0; i < 6; i++) {
            scene.world.removeItemsFromBelt(funnelPos.below());
            scene.world.flapFunnel(funnelPos, false);
            scene.idle(15);
        }

        scene.world.modifyBlockEntityNBT(bothPSI, classPSI, nbt -> {
            nbt.putFloat("Timer", 2);
        });

        // Remove first item train
        scene.world.moveSection(trainElement1, util.vector.of(10, 0, 0), 35);
        scene.world.animateBogey(bogey1, -10, 35);
        scene.world.moveSection(trainElement2, util.vector.of(10, 0, 0), 35);
        scene.world.animateBogey(bogey2, -10, 35);

        scene.idle(20);
        scene.world.hideIndependentSection(trainElement1, Direction.UP);
        scene.world.hideIndependentSection(trainElement2, Direction.UP);

        // Reveal salepoint
        scene.world.showSection(salepointSupport, Direction.WEST);
        scene.idle(10);

        Vec3 marker = util.vector.topOf(stationaryPSIPos);
        AABB bb = new AABB(marker, marker);
        scene.overlay.showControls(new InputWindowElement(marker, Pointing.DOWN).rightClick()
            .withItem(NumismaticsBlocks.SALEPOINT.asStack()), 40);
        scene.idle(6);
        scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb, 1);
        scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.inflate(.5f, 0, .5f), 5);
        scene.idle(5);
        scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, bb.move(0, -.5f, 0)
            .inflate(.5f, .5f, .5f), 95);
        scene.idle(5);

        scene.overlay.showText(70)
            .attachKeyFrame()
            .pointAt(marker)
            .placeNearTarget()
            .colored(PonderPalette.GREEN)
            .text("Select a Portable Storage Interface then place the Salepoint nearby.");
        scene.idle(60);

        scene.world.showSection(salepoint, Direction.EAST);
        scene.idle(15);

        AABB salepointBB = NumismaticsShapes.SALEPOINT.get(Direction.WEST).bounds().move(salepointPos);
        scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb, salepointBB, 20);
        scene.idle(25);

        scene.overlay.showText(80)
            .pointAt(salepointTextVec)
            .attachKeyFrame()
            .placeNearTarget()
            .text("Salepoints are used to buy and sell fluids and items in bulk. As with Vendors, players with access can sneak-use to configure them.");
            //.text("Salepoints restrict the flow of fluids and items through Portable Interfaces.");
        scene.idle(70);

        scene.overlay.showControls(new InputWindowElement(salepointTextVec.add(0, 0.5f, 0), Pointing.DOWN)
            .rightClick()
            .whileSneaking(),
            10);
        scene.idle(15);

        // price should become 4 ingots for 3 spurs
        var menuC = scenex.showContainerMenu(
                5,
                salepointPos,
                NumismaticsBlockEntities.SALEPOINT.get(),
                (be, inv) -> new SalepointConfigMenu(NumismaticsMenuTypes.SALEPOINT_CONFIG.get(), -3, inv, be),
                SalepointConfigScreen::new,
                $ -> Component.translatable("block.numismatics.salepoint")
            )
            .inventoryFiller(inv -> {
                inv.setItem(9, AllItems.BRASS_INGOT.asStack());
            })
            .attachKeyFrame()
            .link();
        scenex.enableScreenOverlayLayer();
        scene.idle(15);

        scenex.modifyCursor(menuC, c -> c
            .setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW)
            .teleport(-20, -30)
            .snapFrame());

        scene.overlay.showText(80)
            .text("Configure a Salepoint like a Vendor: set the filter item and the unit price")
            .independent(50);
        scene.idle(5);

        scenex.modifyCursor(menuC, c -> c.setCursor(Cursor.NORMAL));
        scenex.cursorTarget(ScreenVec.relative(menuC, -20, 60));
        scene.idle(2);
        scenex.cursorTarget(ScreenVec.slotRelative(menuC, 10, 10, SalepointConfigMenu.PLAYER_INV_START_INDEX + 9));
        scene.idle(18);

        // pick up brass ingot
        scenex.clickSlot(menuC, SalepointConfigMenu.PLAYER_INV_START_INDEX + 9);
        scenex.modifyCursor(menuC, $ -> $.setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOWER));
        scenex.cursorTarget(ScreenVec.slotRelative(menuC, 7, 7, SalepointConfigMenu.FILTER_SLOT_INDEX));
        scene.idle(15);

        // set filter
        scenex.clickSlot(menuC, SalepointConfigMenu.FILTER_SLOT_INDEX);
        scene.idle(5);

        // place brass ingot back in inventory
        scenex.cursorTarget(ScreenVec.slotRelative(menuC, 7, 7, SalepointConfigMenu.PLAYER_INV_START_INDEX + 9));
        scene.idle(15);
        scenex.clickSlot(menuC, SalepointConfigMenu.PLAYER_INV_START_INDEX + 9);
        scene.idle(5);

        // set count
        scenex.cursorTarget(ScreenVec.slotRelative(menuC, 10, 10, SalepointConfigMenu.FILTER_SLOT_INDEX));
        scene.idle(15);

        scenex.modifyCursor(menuC, c -> c.setCursor(Cursor.SCROLL_UP));
        for (int i = 1; i < 4; i++) {
            scenex.modifyScreen(menuC, $ -> $.menu().scrollSlot(SalepointConfigMenu.FILTER_SLOT_INDEX, 1, false));
            scene.idle(5);
        }
        scenex.modifyCursor(menuC, c -> c.setCursor(Cursor.NORMAL));
        scene.idle(15);

        // set price
        scenex.cursorTarget(ScreenVec.relative(menuC, 68, 58));
        scene.idle(15);

        scenex.modifyCursor(menuC, c -> c.setCursor(Cursor.SCROLL_UP));

        for (int i = 1; i <= 3; i++) {
            final int finalI = i;
            scenex.modifyScreen(menuC, $ -> $.screen().getVirtualHandle().setPrice(Coin.SPUR, finalI));
            scene.idle(5);
        }

        scenex.modifyCursor(menuC, c -> c.setCursor(Cursor.NORMAL));
        scene.idle(5);

        // move to close button
        scenex.modifyCursor(menuC, c -> c.setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW));
        scenex.cursorTarget(ScreenVec.relative(menuC, 216, 130));
        scene.idle(10);

        // close config screen
        scenex.hideContainerMenu(menuC, 5);
        scenex.disableScreenOverlayLayer();
        scene.idle(10);

        // display result
        scenex.showTextComponent(50)
            .text(salepointTooltip(salepointPos))
            .item(salepointTooltipItem(salepointPos))
            .preserveTextColor()
            .attachKeyFrame()
            .pointAt(salepointTextVec)
            .placeNearTarget();
        scene.idle(10);

        // Spawn brass, fill buffer
        for (int i = 0; i < 6; i++) {
            ElementLink<EntityElement> item = scene.world.createItemEntity(
                util.vector.centerOf(beltStart.above(3)),
                util.vector.of(0, 0, 0),
                brassIngotStack
            );
            scene.idle(13);
            scene.world.modifyEntity(item, Entity::discard);

            scene.world.createItemOnBelt(beltStart, Direction.DOWN, brassIngotStack);
            scene.idle(5);

            if (i == 2) {
                scene.overlay.showText(60)
                    .pointAt(util.vector.blockSurface(stationaryPSIPos, Direction.WEST).add(0, 0.5, 6/16f))
                    .attachKeyFrame()
                    .placeNearTarget()
                    .text("Salepoint-controlled Interfaces don't automatically transfer fluids or items...");
            }

            if (i == 4) {
                scene.world.removeItemsFromBelt(funnelPos.below());
                scene.world.flapFunnel(funnelPos, false);
                scene.world.modifyBlockEntity(salepointPos, SalepointBlockEntity.class, be -> {
                    if (be.getSalepointState() instanceof ItemSalepointState state) {
                        state.getBuffer().copyToBuffer(brassIngotStack);
                    }
                });
            }
        }

        for (int i = 1; i < 4; i++) {
            scene.world.removeItemsFromBelt(funnelPos.below());
            scene.world.flapFunnel(funnelPos, false);
            scene.world.modifyBlockEntity(salepointPos, SalepointBlockEntity.class, be -> {
                if (be.getSalepointState() instanceof ItemSalepointState state) {
                    state.getBuffer().copyToBuffer(brassIngotStack);
                }
            });
            scene.idle(15);

            if (i == 1) {
                scene.overlay.showText(60)
                    .pointAt(util.vector.blockSurface(stationaryPSIPos, Direction.WEST).add(0, -0.5, -16/16f))
                    .placeNearTarget()
                    .text("Instead they are stored in an internal buffer until a transaction is started.");
            }
        }

        scene.idle(20);

        // Reveal second item train
        trainElement1 = scene.world.showIndependentSection(train1, Direction.DOWN);
        scene.world.moveSection(trainElement1, util.vector.of(-14, 0, 0), 0);

        trainElement2 = scene.world.showIndependentSection(train2, Direction.DOWN);
        scene.world.moveSection(trainElement2, util.vector.of(-14, 0, 0), 0);

        scene.world.moveSection(trainElement1, util.vector.of(14, 0, 0), 50);
        scene.world.animateBogey(bogey1, -14, 50);
        scene.world.moveSection(trainElement2, util.vector.of(14, 0, 0), 50);
        scene.world.animateBogey(bogey2, -14, 50);

        scene.idle(50);

        scene.world.modifyBlockEntityNBT(bothPSI, classPSI, nbt -> {
            nbt.putFloat("Distance", 1);
            nbt.putFloat("Timer", 12);
        });

        // describe salepoint menu
        scene.overlay.showText(60)
            .pointAt(salepointTextVec)
            .attachKeyFrame()
            .placeNearTarget()
            .text("Use a Salepoint to initiate a transaction");
        scene.idle(50);

        scene.overlay.showControls(new InputWindowElement(salepointTextVec, Pointing.DOWN)
                .rightClick(),
            10);
        scene.idle(15);

        // open it
        var menuP = scenex.showContainerMenu(
                5,
                salepointPos,
                NumismaticsBlockEntities.SALEPOINT.get(),
                (be, inv) -> new SalepointPurchaseMenu(NumismaticsMenuTypes.SALEPOINT_PURCHASE.get(), -3, inv, be),
                SalepointPurchaseScreen::new,
                $ -> Component.translatable("block.numismatics.salepoint")
            )
            .inventoryFiller(inv -> {
                inv.setItem(9, personalCard());
            })
            .attachKeyFrame()
            .link();
        scenex.enableScreenOverlayLayer();
        scene.idle(15);

        scenex.modifyCursor(menuP, c -> c
            .setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_SLOW)
            .teleport(-20, -30)
            .snapFrame());

        scene.overlay.showText(60)
            .text("Scroll to set unit count...")
            .independent(40)
            .placeNearTarget();
        scene.idle(5);

        scenex.modifyCursor(menuP, c -> c.setCursor(Cursor.NORMAL));
        scenex.cursorTarget(ScreenVec.relative(menuP, 104 + 40 - 3, 81 + 12));
        scene.idle(15);

        scenex.modifyCursor(menuP, c -> c.setCursor(Cursor.SCROLL_UP));

        for (int i = 1; i <= 4; i++) {
            final int finalI = i;
            scenex.modifyScreen(menuP, $ -> $.screen().getVirtualHandle().setUnitCount(finalI));
            scene.idle(5);
        }

        scenex.modifyCursor(menuP, c -> c.setCursor(Cursor.NORMAL));
        scene.idle(10);

        scene.overlay.showText(60)
            .text("...provide a Bank Card...")
            .independent(60)
            .placeNearTarget();
        scene.idle(5);

        scenex.cursorTarget(ScreenVec.slotRelative(menuP, 7, 7, SalepointPurchaseMenu.PLAYER_INV_START_INDEX + 9));
        scene.idle(15);

        scenex.clickSlot(menuP, SalepointPurchaseMenu.PLAYER_INV_START_INDEX + 9);
        scenex.cursorTarget(ScreenVec.slotRelative(menuP, 10, 10, SalepointPurchaseMenu.CARD_SLOT_INDEX));
        scene.idle(15);

        scenex.clickSlot(menuP, SalepointPurchaseMenu.CARD_SLOT_INDEX);
        scenex.modifyScreen(menuP, $ -> $.screen().getVirtualHandle().updateAction());
        scene.idle(5);

        scene.overlay.showText(60)
            .text("...and start the transaction")
            .independent(80)
            .placeNearTarget();
        scene.idle(10);

        scenex.cursorTarget(ScreenVec.slotRelative(menuP, 10 + 20, 2, SalepointPurchaseMenu.CARD_SLOT_INDEX));
        scene.idle(15);
        scene.addKeyframe();

        scenex.modifyScreen(menuP, $ -> $.screen().getVirtualHandle().setClientsideMultiplier(4));

        for (int i = 0; i <= 4; i++) {
            final int finalI = i;
            scenex.modifyScreen(menuP, $ -> $.screen().getVirtualHandle().setClientsideProgress(finalI));
            scene.idle(15);
        }

        scenex.modifyScreen(menuP, $ -> {
            SalepointPurchaseScreen.VirtualHandle handle = $.screen().getVirtualHandle();
            handle.setClientsideProgress(0);
            handle.setClientsideMultiplier(0);
        });

        scene.idle(20);

        // move cursor to close button
        scenex.modifyCursor(menuP, c -> c.setPhysics(CursorPhysicsProperties.EXPRESSIVE_SPATIAL_MEDIUM));
        scenex.cursorTarget(ScreenVec.relative(menuP, 206, 120));
        scene.idle(10);

        // hide screen
        scenex.hideContainerMenu(menuP, 5);
        scenex.disableScreenOverlayLayer();
        scene.idle(10);

        InputWindowElement storedBrass = new InputWindowElement(util.vector.topOf(assembledBarrel), Pointing.DOWN)
            .withItem(AllItems.BRASS_INGOT.asStack(16));
        ((InputWindowElement_Duck) storedBrass).numismatics$showItemCount(true);
        scene.overlay.showControls(
            storedBrass,
            40
        );

        scene.idle(50);

        scene.world.modifyBlockEntityNBT(bothPSI, classPSI, nbt -> {
            nbt.putFloat("Timer", 2);
        });

        // Remove second item train
        scene.world.moveSection(trainElement1, util.vector.of(10, 0, 0), 35);
        scene.world.animateBogey(bogey1, -10, 35);
        scene.world.moveSection(trainElement2, util.vector.of(10, 0, 0), 35);
        scene.world.animateBogey(bogey2, -10, 35);

        scene.idle(20);
        scene.world.hideIndependentSection(trainElement1, Direction.UP);
        scene.world.hideIndependentSection(trainElement2, Direction.UP);

        scene.idle(10);

        scene.overlay.showText(80)
            .attachKeyFrame()
            .text("Fluid (and Fuel) Interfaces work equivalently to Item Interfaces. Use a fluid-containing item like a bucket or bottle to configure them")
            .pointAt(util.vector.blockSurface(stationaryPSIPos, Direction.WEST))
            .placeNearTarget();

        // Swap item utilities for fluid utilities
        Selection[] itemInfraToRemove = new Selection[]{
            smallCog,
            beltIntoBelt,
            gearBoxes,
            beltIntoFunnel,
            stationaryPSI,
            funnel,
            salepointSupport,
            salepoint,
        };
        for (int i = itemInfraToRemove.length - 1; i >= 0; i--) {
            scene.world.hideSection(itemInfraToRemove[i], Direction.UP);
            scene.idle(5);
        }

        scene.idle(20);

        Selection[] fluidToReveal = new Selection[] {
            fluidLargeCog,
            fluidSmallCogAndShaft,
            fluidPipe,
        };

        for (Selection sel : fluidToReveal) {
            ElementLink<WorldSectionElement> sec = scene.world.showIndependentSection(sel, Direction.DOWN);
            scene.world.moveSection(sec, util.vector.of(0, -4, 0), 0);
            scene.idle(5);
        }

        scene.world.showSection(salepointSupport, Direction.DOWN);
        scene.idle(5);
        scene.world.showSection(salepoint, Direction.DOWN);
        scene.idle(5);
    }

    private static Selection mergedSelection(Selection... components) {
        if (components.length == 0) {
            return Selection.of(BoundingBox.fromCorners(Vec3i.ZERO, Vec3i.ZERO));
        } else {
            Selection out = components[0];
            for (int i = 1; i < components.length; i++) {
                out = out.add(components[i]);
            }
            return out;
        }
    }

    private static BiConsumer<PonderScene, List<Component>> salepointTooltip(BlockPos pos) {
        return (scene, tooltip) -> {
            if (scene.getWorld().getBlockEntity(pos) instanceof SalepointBlockEntity sbe) {
                sbe.createTooltipVirtual();
                sbe.addToTooltip(tooltip, false);
            }
        };
    }

    private static Function<PonderScene, ItemStack> salepointTooltipItem(BlockPos pos) {
        return (scene) -> {
            if (scene.getWorld().getBlockEntity(pos) instanceof SalepointBlockEntity sbe) {
                return sbe.getCustomGoggleOverlayStack();
            } else {
                return ItemStack.EMPTY;
            }
        };
    }

    public static void coupleTrain(SceneBuilder scene, BlockPos pos, double distance, Direction direction) {
        scene.addInstruction(PonderInstruction.simple(ponderScene -> {
            PonderWorld world = ponderScene.getWorld();
            world.getBlockEntity(pos, AllBlockEntityTypes.BOGEY.get()).ifPresent(sbte -> {
                if (sbte instanceof StandardBogeyBlockEntity_Duck duck) {
                    duck.numismatics$setCouplingDistance(distance);
                    duck.numismatics$setCouplingDirection(direction);
                } else {
                    Numismatics.LOGGER.warn("Tried to couple ponder train but no bogey found");
                }
            });
        }));
    }

    public static void decoupleTrain(SceneBuilder scene, BlockPos pos) {
        scene.addInstruction(PonderInstruction.simple(ponderScene -> {
            PonderWorld world = ponderScene.getWorld();
            world.getBlockEntity(pos, AllBlockEntityTypes.BOGEY.get()).ifPresent(sbte -> {
                if (sbte instanceof StandardBogeyBlockEntity_Duck duck) {
                    duck.numismatics$setCouplingDistance(-1);
                    duck.numismatics$setCouplingDirection(Direction.UP);
                } else {
                    Numismatics.LOGGER.warn("Tried to decouple ponder train but no bogey found");
                }
            });
        }));
    }

    private static ItemStack personalCard() {
        ItemStack stack = NumismaticsItems.CARDS.get(DyeColor.RED).asStack();
        LocalPlayer player = Minecraft.getInstance().player;
        CardItem.set(stack, player == null ? UUID.fromString("3648354d-2a8d-45aa-87da-aa38293950e1") : player.getUUID());
        return stack;
    }
}
