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

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.instruction.ShowInputInstruction;
import com.simibubi.create.foundation.utility.Pointing;
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlock;
import dev.ithundxr.createnumismatics.mixin.client.AccessorInputWindowElement;
import dev.ithundxr.createnumismatics.ponder.utils.elements.DoubleInputWindowElement;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsIcons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class DepositorScenes {
    public static void depositor(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("depositor", "Using Depositors");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos andesiteDepositor = util.grid.at(3, 1, 2);
        BlockPos brassDepositor = util.grid.at(1, 1, 2);
        
        BlockPos andesiteDepositorAbove = andesiteDepositor.above();
        BlockPos brassDepositorAbove = brassDepositor.above();
        
        BlockPos andesiteDepositorLeft = andesiteDepositor.east();
        BlockPos brassDepositorRight = brassDepositor.west();
        
        BlockPos middleOfLamps = util.grid.at(2, 2, 2);

        scene.world.showSection(util.select.position(andesiteDepositor), Direction.DOWN);
        scene.world.showSection(util.select.position(brassDepositor), Direction.DOWN);
        scene.idle(10);

        scene.world.showSection(util.select.position(andesiteDepositorAbove), Direction.DOWN);
        scene.world.showSection(util.select.position(brassDepositorAbove), Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("Depositors are a way to accept money and produce a redstone output")
                .pointAt(util.vector.topOf(middleOfLamps))
                .placeNearTarget();
        scene.idle(80);

        cycleState(andesiteDepositorAbove, RedstoneLampBlock.LIT, scene);
        cycleState(brassDepositorAbove, RedstoneLampBlock.LIT, scene);
        scene.idle(4);

        cycleState(andesiteDepositorAbove, RedstoneLampBlock.LIT, scene);
        cycleState(brassDepositorAbove, RedstoneLampBlock.LIT, scene);
        scene.idle(10);

        scene.overlay.showText(55)
                .attachKeyFrame()
                .text("This redstone output can be used for many things")
                .pointAt(util.vector.topOf(middleOfLamps))
                .placeNearTarget();
        scene.idle(40);
        
        scene.world.hideSection(util.select.position(andesiteDepositorAbove), Direction.UP);
        scene.world.hideSection(util.select.position(brassDepositorAbove), Direction.UP);
        scene.idle(20);

        scene.world.setBlock(andesiteDepositorAbove, Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH), false);
        scene.world.setBlock(brassDepositorAbove, Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH), false);
        
        scene.world.showSection(util.select.position(andesiteDepositorAbove), Direction.DOWN);
        scene.world.showSection(util.select.position(brassDepositorAbove), Direction.DOWN);
        scene.idle(10);

        cycleState(andesiteDepositorAbove, TrapDoorBlock.OPEN, scene);
        cycleState(brassDepositorAbove, TrapDoorBlock.OPEN, scene);
        scene.idle(8);

        cycleState(andesiteDepositorAbove, TrapDoorBlock.OPEN, scene);
        cycleState(brassDepositorAbove, TrapDoorBlock.OPEN, scene);
        scene.idle(10);
        
        scene.world.hideSection(util.select.position(andesiteDepositorAbove), Direction.UP);
        scene.world.hideSection(util.select.position(brassDepositorAbove), Direction.UP);
        scene.idle(20);

        scene.world.showSection(util.select.position(andesiteDepositorLeft), Direction.DOWN);
        scene.world.showSection(util.select.position(brassDepositorRight), Direction.DOWN);
        scene.world.showSection(util.select.position(andesiteDepositorLeft.above()), Direction.DOWN);
        scene.world.showSection(util.select.position(brassDepositorRight.above()), Direction.DOWN);
        scene.idle(20);

        cycleDoorState(andesiteDepositorLeft, scene);
        cycleDoorState(brassDepositorRight, scene);
        scene.idle(8);

        cycleDoorState(andesiteDepositorLeft, scene);
        cycleDoorState(brassDepositorRight, scene);
        scene.idle(10);

        scene.world.hideSection(util.select.position(andesiteDepositorLeft), Direction.UP);
        scene.world.hideSection(util.select.position(brassDepositorRight), Direction.UP);
        scene.world.hideSection(util.select.position(andesiteDepositorLeft.above()), Direction.UP);
        scene.world.hideSection(util.select.position(brassDepositorRight.above()), Direction.UP);
        scene.idle(20);
        
        scene.world.setBlock(andesiteDepositorLeft, Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.FACING, Direction.EAST), false);
        scene.world.setBlock(brassDepositorRight, Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.FACING, Direction.WEST), false);

        scene.world.showSection(util.select.position(andesiteDepositorLeft), Direction.DOWN);
        scene.world.showSection(util.select.position(brassDepositorRight), Direction.DOWN);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("They can also be locked using a redstone input, preventing them from accepting money")
                .pointAt(util.vector.topOf(middleOfLamps))
                .placeNearTarget();
        scene.idle(20);
        
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(andesiteDepositor), Pointing.DOWN).showing(NumismaticsIcons.I_COIN_COG_RED_LINE), 40);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(brassDepositor), Pointing.DOWN).showing(NumismaticsIcons.I_COIN_COG_RED_LINE), 40);
        
        cycleState(andesiteDepositorLeft, RepeaterBlock.POWERED, scene);
        cycleState(brassDepositorRight, RepeaterBlock.POWERED, scene);
        
        cycleState(andesiteDepositor, AbstractDepositorBlock.LOCKED, scene);
        cycleState(brassDepositor, AbstractDepositorBlock.LOCKED, scene);
        
        scene.idle(40);

        cycleState(andesiteDepositorLeft, RepeaterBlock.POWERED, scene);
        cycleState(brassDepositorRight, RepeaterBlock.POWERED, scene);

        cycleState(andesiteDepositor, AbstractDepositorBlock.LOCKED, scene);
        cycleState(brassDepositor, AbstractDepositorBlock.LOCKED, scene);
    }

    public static void depositorPricing(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("depositor_pricing", "Depositor Pricing");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos depositor = util.grid.at(2, 1, 2);

        BlockPos redstoneLamp = util.grid.at(2, 1, 3);

        scene.world.showSection(util.select.position(depositor), Direction.DOWN);
        scene.idle(10);

        scene.world.showSection(util.select.position(redstoneLamp), Direction.DOWN);
        scene.idle(10);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("A depositor’s price can be set in its UI")
                .pointAt(util.vector.topOf(depositor))
                .placeNearTarget();
        scene.idle(80);

        InputWindowElement element = new InputWindowElement(util.vector.topOf(depositor), Pointing.DOWN);
        ((AccessorInputWindowElement) element).numsismatics$setKey(Numismatics.asResource("amount_spaced_1x"));
        scene.addInstruction(new ShowInputInstruction(element, 77));
        
        changeIcon(scene, element, NumismaticsIcons.I_COIN_SPUR);
        changeIcon(scene, element, NumismaticsIcons.I_COIN_BEVEL);
        changeIcon(scene, element, NumismaticsIcons.I_COIN_SPROCKET);
        changeIcon(scene, element, NumismaticsIcons.I_COIN_COG);
        changeIcon(scene, element, NumismaticsIcons.I_COIN_CROWN);

        changeIcon(scene, element, NumismaticsIcons.I_COIN_SUN);

        changeIcon(scene, element, NumismaticsIcons.I_COIN_CROWN);
        changeIcon(scene, element, NumismaticsIcons.I_COIN_COG);
        changeIcon(scene, element, NumismaticsIcons.I_COIN_SPROCKET);
        changeIcon(scene, element, NumismaticsIcons.I_COIN_BEVEL);
        changeIcon(scene, element, NumismaticsIcons.I_COIN_SPUR);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("The andesite depositor can only set a simple price in single coins, and will only check the player’s hand for a coin")
                .pointAt(util.vector.topOf(depositor))
                .placeNearTarget();
        scene.idle(80);

        showIcon(scene, util.vector.topOf(depositor), "amount1x", NumismaticsIcons.I_COIN_COG_RED_LINE, 40);
        scene.effects.indicateRedstone(depositor);
        scene.idle(50);

        showIcon(scene, util.vector.topOf(depositor), "amount1x", NumismaticsIcons.I_COIN_SPUR, 40);
        indicateSuccess(scene, depositor, redstoneLamp);

        scene.world.hideSection(util.select.position(depositor), Direction.UP);
        scene.idle(20);
        
        scene.world.setBlock(depositor, NumismaticsBlocks.BRASS_DEPOSITOR.getDefaultState(),false);

        scene.world.showSection(util.select.position(depositor), Direction.DOWN);
        scene.idle(20);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("The brass depositor can accept a complex price using multiple coin values, and will check the player’s whole inventory for the appropriate change")
                .pointAt(util.vector.topOf(depositor))
                .placeNearTarget();
        scene.idle(80);

        showIcon(scene, util.vector.topOf(depositor),
                "amount3x", NumismaticsIcons.I_COIN_COG,
                "amount2x", NumismaticsIcons.I_COIN_SPROCKET,
                40
        );
        indicateSuccess(scene, depositor, redstoneLamp);
    }

    // <--------------------------------------------> Utilities <-------------------------------------------->
    private static <T extends Comparable<T>> void cycleState(BlockPos pos, Property<T> property, SceneBuilder scene) {
        scene.world.modifyBlock(pos, state -> state.cycle(property), false);
    }
    
    private static void cycleDoorState(BlockPos doorPos, SceneBuilder scene) {
        cycleState(doorPos, DoorBlock.OPEN, scene);
        cycleState(doorPos.above(), DoorBlock.OPEN, scene);
    }

    private static void indicateSuccess(SceneBuilder scene, BlockPos depositorPos, BlockPos lampPos) {
        scene.effects.indicateSuccess(depositorPos);

        cycleState(depositorPos, AbstractDepositorBlock.LOCKED, scene);
        cycleState(lampPos, RedstoneLampBlock.LIT, scene);
        scene.idle(50);

        cycleState(depositorPos, AbstractDepositorBlock.LOCKED, scene);
        cycleState(lampPos, RedstoneLampBlock.LIT, scene);
        scene.idle(10);
    }

    private static InputWindowElement createElement(Vec3 sceneSpace, String sharedTextValue, AllIcons icon) {
        InputWindowElement element = new InputWindowElement(sceneSpace, Pointing.DOWN).showing(icon);
        ((AccessorInputWindowElement) element).numsismatics$setKey(Numismatics.asResource(sharedTextValue));
        
        return element;
    }

    private static DoubleInputWindowElement createElement(Vec3 sceneSpace, String firstSharedTextValue, AllIcons firstIcon, String secondSharedTextValue, AllIcons secondIcon) {
        InputWindowElement element1 = createElement(sceneSpace, firstSharedTextValue, firstIcon);
        InputWindowElement element2 = createElement(sceneSpace, secondSharedTextValue, secondIcon);

        return new DoubleInputWindowElement(sceneSpace, Pointing.DOWN, element1, element2);
    }

    private static void showIcon(SceneBuilder scene, Vec3 sceneSpace, String sharedTextValue, AllIcons icon, int duration) {
        scene.overlay.showControls(createElement(sceneSpace, sharedTextValue, icon), duration);
    }

    private static void showIcon(SceneBuilder scene, Vec3 sceneSpace, String firstSharedTextValue, AllIcons firstIcon, String secondSharedTextValue, AllIcons secondIcon, int duration) {
        scene.overlay.showControls(createElement(sceneSpace, firstSharedTextValue, firstIcon, secondSharedTextValue, secondIcon), duration);
    }
    
    private static void changeIcon(SceneBuilder scene, InputWindowElement element, AllIcons icon) {
        scene.addInstruction(s -> element.showing(icon));
        scene.idle(7);
    }
}
