/*
 * Numismatics
 * Copyright (c) 2024-2025 The Railways Team
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

import dev.ithundxr.silk.ChangelogText
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.kotlin.dsl.named

architectury.neoForge()

loom {
    val common = project(":common")
    accessWidenerPath = project(":common").loom.accessWidenerPath

    if (findProject(":fabric") == null) {
        runs {
            create("datagen") {
                data()

                name = "Minecraft Data"
                programArgs("--all", "--mod", "numismatics")
                programArgs("--output", common.file("src/generated/resources").absolutePath)
                programArgs("--existing", common.file("src/main/resources").absolutePath)
                programArgs("--existing-mod", "create")

                environmentVariable("DATAGEN", "TRUE")
            }
        }
    }
}

tasks.named<RemapJarTask>("remapJar") {
    atAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
}

dependencies {
    neoForge("net.neoforged:neoforge:${"neoforge_version"()}")
    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionNeoForge")) { isTransitive = false }

    // Create and its dependencies
    modImplementation("com.simibubi.create:create-${"minecraft_version"()}:${"create_neoforge_version"()}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:ponder-neoforge:${"ponder_version"()}+mc${"minecraft_version"()}")
    modImplementation("com.tterrag.registrate:Registrate:${"registrate_neoforge_version"()}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${"minecraft_version"()}:${"flywheel_version"()}")
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${"minecraft_version"()}:${"flywheel_version"()}")

    modLocalRuntime("dev.emi:emi-neoforge:${"emi_version"()}")

    // Carry On
    modCompileOnly("tschipp.carryon:carryon-neoforge-${"minecraft_version"()}:${"carryon_neoforge_version"()}")
    if ("enable_carryon"().toBoolean()) {
        modLocalRuntime("tschipp.carryon:carryon-neoforge-${"minecraft_version"()}:${"carryon_neoforge_version"()}")
    }

    // Create Crafts and Additions
    modCompileOnly("maven.modrinth:createaddition:${"createaddition_neoforge_version"()}")
    if ("enable_createaddition"().toBoolean()) {
        modLocalRuntime("maven.modrinth:createaddition:${"createaddition_neoforge_version"()}")
    }

    // CC: Tweaked
    modCompileOnly("cc.tweaked:cc-tweaked-${"minecraft_version"()}-core-api:${"cc_version"()}")
    modCompileOnly("cc.tweaked:cc-tweaked-${"minecraft_version"()}-forge-api:${"cc_version"()}")
    if ("enable_cc"().toBoolean()) {
        modLocalRuntime("cc.tweaked:cc-tweaked-${"minecraft_version"()}-forge:${"cc_version"()}")
    }

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")!!)!!
    implementation(include("io.github.llamalad7:mixinextras-fabric:${"mixin_extras_version"()}")!!)!!
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
