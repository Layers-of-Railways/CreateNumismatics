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

import dev.ithundxr.silk.ChangelogText

architectury.fabric()

loom {
    val common = project(":common")
    accessWidenerPath = common.loom.accessWidenerPath

    runs {
        create("datagen") {
            client()

            name = "Minecraft Data"
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${common.file("src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.modid=numismatics")
            vmArg("-Dporting_lib.datagen.existing_resources=${common.file("src/main/resources")}")
        }

        getByName("client") {
            programArg("--username=Dev")
        }
    }
}

repositories {
    // mavens for Fabric-exclusives
    maven("https://api.modrinth.com/maven") // LazyDFU
    maven("https://maven.terraformersmc.com/releases/") // Mod Menu, EMI
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // Forge config api port
    maven("https://maven.cafeteria.dev/releases") // Fake Player API
    maven("https://maven.jamieswhiteshirt.com/libs-release") // Reach Entity Attributes
    maven("https://jitpack.io/") // Mixin Extras, Fabric ASM
    maven("https://maven.siphalor.de/") { // Amecs API (required by Carry On)
        name = "Siphalor's Maven"
    }
    maven("https://squiddev.cc/maven/") {// CC Tweaked
        content {
            includeGroup("cc.tweaked")
        }
    }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) { isTransitive = false }

    // dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:${"fabric_api_version"()}")

    // Create - dependencies are added transitively
    modImplementation("com.simibubi.create:create-fabric-${"minecraft_version"()}:${"create_fabric_version"()}")

    // deprecated modules @ runtime only
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api-deprecated:${"fabric_api_version"()}")

    // Development QOL
    modLocalRuntime("maven.modrinth:lazydfu:${"lazydfu_version"()}")
    modLocalRuntime("com.terraformersmc:modmenu:${"modmenu_version"()}")
    modLocalRuntime("maven.modrinth:jade:${"jade_version"()}")

    modCompileOnly("dev.emi:emi-fabric:${"emi_version"()}:api")
    modLocalRuntime("dev.emi:emi-fabric:${"emi_version"()}")

    // Steam 'n' Rails
    val buildNumber = if ("snr_build_number"() != "null") "-build." + "snr_build_number"() else ""
    modCompileOnly("com.railwayteam.railways:Steam_Rails-fabric-${"minecraft_version"()}:${"snr_version"()}+fabric-mc${"minecraft_version"() + buildNumber}") { isTransitive = false }
    if ("enable_snr"().toBoolean()) {
        modLocalRuntime("com.railwayteam.railways:Steam_Rails-fabric-${"minecraft_version"()}:${"snr_version"()}+fabric-mc${"minecraft_version"() + buildNumber}") { isTransitive = false }
    }

    // Carry On
    modCompileOnly("tschipp.carryon:carryon-fabric-${"minecraft_version"()}:${"carryon_forge_version"()}")
    if ("enable_carryon"().toBoolean()) {
        modLocalRuntime("tschipp.carryon:carryon-fabric-${"minecraft_version"()}:${"carryon_forge_version"()}")
    }

    // Create Crafts and Additions
    modCompileOnly("maven.modrinth:createaddition:${"createaddition_fabric_version"()}")
    modCompileOnly("teamreborn:energy:2.3.0") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    if ("enable_createaddition"().toBoolean()) {
        modLocalRuntime("maven.modrinth:createaddition:${"createaddition_fabric_version"()}")
        modLocalRuntime("teamreborn:energy:2.3.0") {
            exclude(group = "net.fabricmc.fabric-api")
        }
    }

    // CC: Tweaked
    modCompileOnly("cc.tweaked:cc-tweaked-${"minecraft_version"()}-fabric-api:${"cc_version"()}")
    if ("enable_cc"().toBoolean()) {
        modLocalRuntime("cc.tweaked:cc-tweaked-${"minecraft_version"()}-fabric:${"cc_version"()}")
    }
}

publishMods {
    file = tasks.remapJar.get().archiveFile
    version.set(project.version.toString())
    changelog = ChangelogText.getChangelogText(rootProject).toString()
    type = STABLE
    displayName = "Numismatics ${"mod_version"()} Fabric ${"minecraft_version"()}"
    modLoaders.add("fabric")
    modLoaders.add("quilt")

    curseforge {
        projectId = "curseforge_id"()
        accessToken = System.getenv("CURSEFORGE_TOKEN")
        minecraftVersions.add("minecraft_version"())

        requires {
            slug = "create-fabric"
        }
    }

    modrinth {
        projectId = "modrinth_id"()
        accessToken = System.getenv("MODRINTH_TOKEN")
        minecraftVersions.add("minecraft_version"())

        requires {
            slug = "create-fabric"
        }
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}