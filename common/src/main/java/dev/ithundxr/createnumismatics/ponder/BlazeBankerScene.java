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
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlazeBankerScene {
    public static void banker(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("blaze_banker", "Banking with Blazes");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos blazeBurner = util.grid.at(2, 1, 2);
        scene.world.showSection(util.select.position(blazeBurner), Direction.DOWN);
        scene.idle(10);
        
        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("Create a Blaze Banker by applying a Banking Guide to a blaze burner")
                .pointAt(util.vector.blockSurface(blazeBurner, Direction.WEST))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(blazeBurner), Pointing.DOWN).leftClick()
                .withItem(NumismaticsItems.BANKING_GUIDE.asStack()), 15);
        scene.idle(7);
        scene.world.setBlock(blazeBurner, NumismaticsBlocks.BLAZE_BANKER.getDefaultState(), false);
        scene.idle(20);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("The Blaze Banker is a way for players to manage money without using their own account")
                .pointAt(util.vector.blockSurface(blazeBurner, Direction.WEST))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("To bind a card to a Banker, place it into its card slot.")
                .pointAt(util.vector.blockSurface(blazeBurner, Direction.WEST))
                .placeNearTarget();
        scene.idle(80);

        scene.effects.indicateSuccess(blazeBurner);
    }
}
