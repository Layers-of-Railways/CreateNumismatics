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
import dev.ithundxr.createnumismatics.content.depositor.AbstractDepositorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;

public class DepositorScene {
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

        scene.world.modifyBlock(andesiteDepositorAbove, state -> state.cycle(RedstoneLampBlock.LIT), false);
        scene.world.modifyBlock(brassDepositorAbove, state -> state.cycle(RedstoneLampBlock.LIT), false);
        scene.idle(4);

        scene.world.modifyBlock(andesiteDepositorAbove, state -> state.cycle(RedstoneLampBlock.LIT), false);
        scene.world.modifyBlock(brassDepositorAbove, state -> state.cycle(RedstoneLampBlock.LIT), false);
        scene.idle(10);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("This redstone output can be used for many things")
                .pointAt(util.vector.topOf(middleOfLamps))
                .placeNearTarget();
        scene.idle(80);
        
        scene.world.hideSection(util.select.position(andesiteDepositorAbove), Direction.UP);
        scene.world.hideSection(util.select.position(brassDepositorAbove), Direction.UP);
        scene.idle(20);

        scene.world.setBlock(andesiteDepositorAbove, Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH), false);
        scene.world.setBlock(brassDepositorAbove, Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING, Direction.SOUTH), false);
        
        scene.world.showSection(util.select.position(andesiteDepositorAbove), Direction.DOWN);
        scene.world.showSection(util.select.position(brassDepositorAbove), Direction.DOWN);
        scene.idle(10);

        scene.world.modifyBlock(andesiteDepositorAbove, state -> state.cycle(TrapDoorBlock.OPEN), false);
        scene.world.modifyBlock(brassDepositorAbove, state -> state.cycle(TrapDoorBlock.OPEN), false);
        scene.idle(8);

        scene.world.modifyBlock(andesiteDepositorAbove, state -> state.cycle(TrapDoorBlock.OPEN), false);
        scene.world.modifyBlock(brassDepositorAbove, state -> state.cycle(TrapDoorBlock.OPEN), false);
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
        scene.idle(40);
        
        scene.world.modifyBlock(andesiteDepositorLeft, state -> state.cycle(RepeaterBlock.POWERED), false);
        scene.world.modifyBlock(brassDepositorRight, state -> state.cycle(RepeaterBlock.POWERED), false);
        
        scene.world.modifyBlock(andesiteDepositor, state -> state.cycle(AbstractDepositorBlock.LOCKED), false);
        scene.world.modifyBlock(brassDepositor, state -> state.cycle(AbstractDepositorBlock.LOCKED), false);
        
        scene.idle(30);

        scene.world.modifyBlock(andesiteDepositorLeft, state -> state.cycle(RepeaterBlock.POWERED), false);
        scene.world.modifyBlock(brassDepositorRight, state -> state.cycle(RepeaterBlock.POWERED), false);

        scene.world.modifyBlock(andesiteDepositor, state -> state.cycle(AbstractDepositorBlock.LOCKED), false);
        scene.world.modifyBlock(brassDepositor, state -> state.cycle(AbstractDepositorBlock.LOCKED), false);
        
        scene.idle(10);
    }
    
    private static void cycleDoorState(BlockPos doorPos, SceneBuilder scene) {
        scene.world.modifyBlock(doorPos, state -> state.cycle(DoorBlock.OPEN), false);
        scene.world.modifyBlock(doorPos.above(), state -> state.cycle(DoorBlock.OPEN), false);
    }
}
