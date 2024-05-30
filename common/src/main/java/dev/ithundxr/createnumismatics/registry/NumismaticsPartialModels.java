/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.registry;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.Create;
import dev.ithundxr.createnumismatics.Numismatics;

public class NumismaticsPartialModels {
    public static final PartialModel
        TOP_HAT = entity("tophat")
        ;

    private static PartialModel createBlock(String path) {
        return new PartialModel(Create.asResource("block/" + path));
    }

    private static PartialModel block(String path) {
        return new PartialModel(Numismatics.asResource("block/" + path));
    }

    private static PartialModel entity(String path) {
        return new PartialModel(Numismatics.asResource("entity/" + path));
    }


    @SuppressWarnings("EmptyMethod")
    public static void init() {
    }
}
