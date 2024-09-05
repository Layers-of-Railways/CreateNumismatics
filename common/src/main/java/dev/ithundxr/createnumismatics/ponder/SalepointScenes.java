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
import com.simibubi.create.foundation.ponder.Selection;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SalepointScenes {
    public static void item(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("salepoint_item", "Using Salepoints");
        scene.configureBasePlate(1, 0, 12);
        scene.scaleSceneView(.60f);
        scene.setSceneOffsetY(-1);
        scene.showBasePlate();
        scene.idle(10);

        Selection salepoint = util.select.position(11, 3, 4);
        Selection portableInterface = util.select.position(7, 2, 4);
        Selection funnel = util.select.position(7, 2, 3);

        Selection beltIntoFunnel = util.select.fromTo(7, 1, 2, 7, 1, 3);
        Selection beltIntoBelt = util.select.fromTo(12, 1, 2, 8, 1, 2);
        
        Selection gearBoxes = util.select.fromTo(8, 1, 3, 9, 1, 3);
        Selection smallCog = util.select.position(12, 1, 3);
        Selection largeCog = util.select.position(13, 0, 3);

        Selection train1 = util.select.fromTo(12, 2, 4, 8, 3, 7);
        Selection train2 = util.select.fromTo(7, 2, 4, 4, 3, 7);
        Selection train2a = util.select.fromTo(7, 2, 7, 4, 3, 10);
        
        // Show tracks
        for (int i = 0; i <= 13; i++) {
            scene.world.showSection(util.select.position(i, 1, 6), Direction.DOWN);
            scene.idle(1);
        }
        
        scene.world.showSection(largeCog, Direction.DOWN);
        scene.idle(10);
        scene.world.showSection(smallCog, Direction.DOWN);
        scene.idle(10);

        scene.world.showSection(beltIntoBelt, Direction.DOWN);
        scene.idle(10);
        scene.world.showSection(gearBoxes, Direction.DOWN);
        scene.idle(10);
        scene.world.showSection(beltIntoFunnel, Direction.DOWN);
        scene.idle(10);

        scene.world.showSection(portableInterface, Direction.DOWN);
        scene.idle(10);
        scene.world.showSection(funnel, Direction.DOWN);
        scene.idle(10);
    }
}
