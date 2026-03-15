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

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.instruction.ShowInputInstruction;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Pointing;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlock;
import dev.ithundxr.createnumismatics.mixin.client.AccessorInputWindowElement;
import dev.ithundxr.createnumismatics.ponder.utils.elements.DoubleInputWindowElement;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class DepositorScenes {
    public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("depositor_intro", "Using Depositors");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos depositor = util.grid.at(2, 1, 2);
        BlockPos redstoneLamp = depositor.above();

        scene.world.showSection(util.select.position(depositor), Direction.DOWN);
        scene.idle(10);
        scene.world.showSection(util.select.position(redstoneLamp), Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(70)
            .text("Depositors emit a redstone pulse in exchange for payment.")
            .attachKeyFrame()
            .pointAt(util.vector.centerOf(depositor))
            .placeNearTarget();
        scene.idle(30);

        depositorSuccess(depositor, redstoneLamp, Coin.SPUR, scene, util);
    }

    public static void redstone(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("depositor_redstone", "Redstone Control");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos depositor = util.grid.at(2, 1, 2);
        BlockPos redstoneLamp = depositor.above();
        BlockPos repeater = util.grid.at(1, 1, 2);
        BlockPos lever = util.grid.at(0, 1, 2);

        scene.world.showSection(util.select.position(depositor), Direction.DOWN);
        scene.idle(10);
        scene.world.showSection(util.select.position(redstoneLamp), Direction.DOWN);
        scene.idle(10);

        scene.world.showSection(util.select.fromTo(lever, repeater), Direction.EAST);

        scene.overlay.showText(70)
            .attachKeyFrame()
            .text("Redstone power will lock depositors.")
            .pointAt(util.vector.centerOf(depositor))
            .placeNearTarget();
        scene.idle(80);

        // lock
        scene.effects.indicateRedstone(lever);
        cycleState(lever, LeverBlock.POWERED, scene);
        cycleState(repeater, RepeaterBlock.POWERED, scene);
        scene.idle(2);
        cycleState(depositor, AbstractDepositorBlock.LOCKED, scene);
        scene.idle(20);

        // failure
        depositorFailure(depositor, Coin.SPUR, scene, util);
        scene.idle(10);

        // unlock
        scene.effects.indicateRedstone(lever);
        cycleState(lever, LeverBlock.POWERED, scene);
        cycleState(repeater, RepeaterBlock.POWERED, scene);
        scene.idle(2);
        cycleState(depositor, AbstractDepositorBlock.LOCKED, scene);
        scene.idle(20);

        // success
        depositorSuccess(depositor, redstoneLamp, Coin.COG, scene, util);
    }

    public static void pricing(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("depositor_pricing", "Depositor Pricing");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos depositor = util.grid.at(2, 1, 2);
        BlockPos redstoneLamp = util.grid.at(3, 1, 2);

        scene.world.showSection(util.select.position(depositor), Direction.DOWN);
        scene.idle(10);
        scene.world.showSection(util.select.position(redstoneLamp), Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(70)
            .attachKeyFrame()
            .text("Andesite depositors can accept any single coin.")
            .pointAt(util.vector.topOf(depositor))
            .placeNearTarget();
        scene.idle(80);

        // andesite price cascade
        int cascadeCount = Coin.values().length*2 - 1;
        int cascadeInterval = 7;
        InputWindowElement price = createElement(
            util.vector.topOf(depositor),
            "amount_spaced_1x", Coin.SPUR.getIcon()
        );
        scene.addInstruction(new ShowInputInstruction(price, cascadeInterval * cascadeCount));

        for (int i = 0; i < cascadeCount; i++) {
            int coinIndex = i < Coin.values().length ? i : cascadeCount - i - 1;
            Coin coin = Coin.values()[coinIndex];
            changeIcon(scene, price, coin.getIcon(), cascadeInterval);
        }
        scene.idle(10);

        depositorFailure(depositor, Coin.BEVEL, scene, util);
        scene.idle(10);

        depositorSuccess(depositor, redstoneLamp, Coin.SPUR, scene, util);
        scene.idle(10);

        // convert to brass
        scene.world.hideSection(util.select.position(depositor), Direction.UP);
        scene.idle(20);
        scene.world.modifyBlock(depositor, state -> BlockHelper.copyProperties(state, NumismaticsBlocks.BRASS_DEPOSITOR.getDefaultState()), false);
        scene.world.showSection(util.select.position(depositor), Direction.DOWN);
        scene.idle(20);

        scene.overlay.showText(70)
            .attachKeyFrame()
            .text("Brass depositors can accept complex prices consisting of multiple coins.")
            .pointAt(util.vector.topOf(depositor))
            .placeNearTarget();
        scene.idle(80);

        // brass price cascade
        int[][] values = {
            {1, 6},
            {2, 5},
            {3, 4},
            {4, 3},
            {5, 2},
            {6, 1},
            {5, 2},
            {4, 3},
            {3, 4},
            {2, 5},
            {1, 6},
        };
        DoubleInputWindowElement combinedPrice = createElement(
            util.vector.topOf(depositor),
            "amount_spaced_1x", Coin.SPROCKET.getIcon(),
            "amount_spaced_1x", Coin.COG.getIcon()
        );
        scene.addInstruction(new ShowInputInstruction(combinedPrice, cascadeInterval * values.length));

        for (int[] valuePair : values) {
            int sprocketCount = valuePair[0];
            int cogCount = valuePair[1];
            changeAmount(scene, combinedPrice, sprocketCount, cogCount, cascadeInterval);
        }
        scene.idle(10);

        depositorSuccess(depositor, redstoneLamp, NumismaticsItems.CARDS.get(DyeColor.RED).asStack(), scene, util);
        scene.idle(10);
    }

    // <--------------------------------------------> Utilities <-------------------------------------------->
    private static <T extends Comparable<T>> void cycleState(BlockPos pos, Property<T> property, SceneBuilder scene) {
        scene.world.modifyBlock(pos, state -> state.cycle(property), false);
    }

    private static void depositorSuccess(BlockPos depositor, BlockPos redstoneLamp, Coin coin, SceneBuilder scene, SceneBuildingUtil util) {
        depositorSuccess(depositor, redstoneLamp, coin.asStack(), scene, util);
    }

    private static void depositorSuccess(BlockPos depositor, BlockPos redstoneLamp, ItemStack stack, SceneBuilder scene, SceneBuildingUtil util) {
        Vec3 depositorFace = util.vector.blockSurface(depositor, Direction.NORTH);

        scene.overlay.showControls(new InputWindowElement(depositorFace, Pointing.RIGHT)
                .withItem(stack)
                .rightClick(),
            40
        );
        scene.idle(6);

        scene.effects.indicateSuccess(depositor.north());
        cycleState(redstoneLamp, RedstoneLampBlock.LIT, scene);
        scene.idle(2 + 4);
        cycleState(redstoneLamp, RedstoneLampBlock.LIT, scene);

        scene.idle(28);
    }

    private static void depositorFailure(BlockPos depositor, Coin coin, SceneBuilder scene, SceneBuildingUtil util) {
        Vec3 depositorFace = util.vector.blockSurface(depositor, Direction.NORTH);

        scene.overlay.showControls(new InputWindowElement(depositorFace, Pointing.RIGHT)
                .withItem(coin.asStack())
                .rightClick(),
            40
        );
        scene.idle(6);

        scene.effects.indicateRedstone(depositor.north());
        scene.idle(34);
    }



    private static InputWindowElement createElement(Vec3 sceneSpace, String sharedTextValue, AllIcons icon) {
        InputWindowElement element = new InputWindowElement(sceneSpace, Pointing.DOWN).showing(icon);
        ((AccessorInputWindowElement) element).numismatics$setKey(Numismatics.asResource(sharedTextValue));

        return element;
    }

    private static DoubleInputWindowElement createElement(Vec3 sceneSpace, String firstSharedTextValue, AllIcons firstIcon, String secondSharedTextValue, AllIcons secondIcon) {
        InputWindowElement element1 = createElement(sceneSpace, firstSharedTextValue, firstIcon);
        InputWindowElement element2 = createElement(sceneSpace, secondSharedTextValue, secondIcon);

        return new DoubleInputWindowElement(sceneSpace, Pointing.DOWN, element1, element2);
    }

    private static void changeIcon(SceneBuilder scene, InputWindowElement element, AllIcons icon, int interval) {
        scene.addInstruction(s -> element.showing(icon));
        scene.idle(interval);
    }

    private static void changeAmount(SceneBuilder scene, DoubleInputWindowElement element, int firstAmount, int secondAmount, int interval) {
        AccessorInputWindowElement firstElementAccessor = (AccessorInputWindowElement) element.firstElement;
        AccessorInputWindowElement secondElementAccessor = (AccessorInputWindowElement) element.secondElement;

        scene.addInstruction(s -> {
            firstElementAccessor.numismatics$setKey(Numismatics.asResource("amount_spaced_" + firstAmount + "x"));
            secondElementAccessor.numismatics$setKey(Numismatics.asResource("amount_spaced_" + secondAmount + "x"));
        });
        scene.idle(interval);
    }
}
