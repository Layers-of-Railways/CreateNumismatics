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

architectury.neoForge()

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath

    neoForge {
        // TODO
        //mixinConfig("numismatics-common.mixins.json")
        //mixinConfig("numismatics.mixins.json")

        //convertAccessWideners = true
        //extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
    }
}

dependencies {
    neoForge("net.neoforged:neoforge:${"neoforge_version"()}")
    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionForge")) { isTransitive = false }

    // Create and its dependencies
    modImplementation("com.simibubi.create:create-${"minecraft_version"()}:${"create_neoforge_version"()}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:Ponder-NeoForge-${"minecraft_version"()}:${"ponder_version"()}")
    modImplementation("com.tterrag.registrate:Registrate:${"registrate_neoforge_version"()}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${"minecraft_version"()}:${"flywheel_version"()}")
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${"minecraft_version"()}:${"flywheel_version"()}")

    modLocalRuntime("dev.emi:emi-neoforge:${"emi_version"()}")

    // Carry On
    modCompileOnly("tschipp.carryon:carryon-neoforge-${"minecraft_version"()}:${"carryon_neoforge_version"()}")
    if ("enable_carryon"().toBoolean()) {
        modLocalRuntime("tschipp.carryon:carryon-neoforge-${"minecraft_version"()}:${"carryon_neoforge_version"()}")
    }
}

publishMods {
    file = tasks.remapJar.get().archiveFile
    version.set(project.version.toString())
    changelog = ChangelogText.getChangelogText(rootProject).toString()
    type = STABLE
    displayName = "Numismatics ${"mod_version"()} Forge ${"minecraft_version"()}"
    modLoaders.add("forge")
    modLoaders.add("neoforge")

    curseforge {
        projectId = "curseforge_id"()
        accessToken = System.getenv("CURSEFORGE_TOKEN")
        minecraftVersions.add("minecraft_version"())

        requires {
            slug = "create"
        }
    }

    modrinth {
        projectId = "modrinth_id"()
        accessToken = System.getenv("MODRINTH_TOKEN")
        minecraftVersions.add("minecraft_version"())

        requires {
            slug = "create"
        }
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
