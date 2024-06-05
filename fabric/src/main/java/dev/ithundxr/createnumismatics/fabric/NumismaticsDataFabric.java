/*
 * Numismatics
 * Copyright (c) 2023-2024 The Railways Team
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

package dev.ithundxr.createnumismatics.fabric;

import dev.ithundxr.createnumismatics.Numismatics;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class NumismaticsDataFabric implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        Path numismaticsResources = Paths.get(System.getProperty(ExistingFileHelper.EXISTING_RESOURCES));
        ExistingFileHelper helper = new ExistingFileHelper(
            Set.of(numismaticsResources), Set.of("create"), false, null, null
        );
        FabricDataGenerator.Pack pack = gen.createPack();
        Numismatics.registrate().setupDatagen(pack, helper);
        Numismatics.gatherData(pack);
    }
}
