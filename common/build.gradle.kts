/*
 * Numismatics
 * Copyright (c) 2024-2026 The Railways Team
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

architectury {
    common {
        for(p in rootProject.subprojects) {
            if(p != project) {
                this@common.add(p.name)
            }
        }
    }
}

loom {
    accessWidenerPath = file("src/main/resources/numismatics.accesswidener")
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
    // Compile against Create Fabric in common
    // beware of differences across platforms!
    // dependencies must also be pulled in to minimize problems, from remapping issues to compile errors.
    // All dependencies except Flywheel and Registrate are NOT safe to use!
    // Flywheel and Registrate must also be used carefully due to differences.
    //modCompileOnly("com.simibubi.create:create-fabric:${"create_fabric_version"()}")

    // Create and its dependencies
    modImplementation("com.simibubi.create:create-${"minecraft_version"()}:${"create_neoforge_version"()}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:ponder-neoforge:${"ponder_version"()}+mc${"minecraft_version"()}")
    modImplementation("com.tterrag.registrate:Registrate:${"registrate_neoforge_version"()}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${"minecraft_version"()}:${"flywheel_version"()}")
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${"minecraft_version"()}:${"flywheel_version"()}")

    // Needed for compiling
    compileOnly("net.neoforged:neoforge:${"neoforge_version"()}")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // required for proper remapping and compiling
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${"fabric_api_version"()}")

    // Mixin Extras
    implementation(annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")!!)

    // Steam 'n' Rails
    val buildNumber = if ("snr_build_number"() != "null") "-build." + "snr_build_number"() else ""
    modCompileOnly("com.railwayteam.railways:Steam_Rails-common-${"minecraft_version"()}:${"snr_version"()}+common-mc${"minecraft_version"() + buildNumber}") { isTransitive = false }

    // Carry On
    //modCompileOnly("tschipp.carryon:carryon-fabric-${"minecraft_version"()}:${"carryon_fabric_version"()}")
    modCompileOnly("tschipp.carryon:carryon-neoforge-${"minecraft_version"()}:${"carryon_neoforge_version"()}")

    // CC: Tweaked
    compileOnly("cc.tweaked:cc-tweaked-${"minecraft_version"()}-common-api:${"cc_version"()}")

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")!!)
}


tasks.processResources {
    // don't add development or to-do files into built jar
    exclude("**/*.bbmodel", "**/*.lnk", "**/*.xcf", "**/*.md", "**/*.txt", "**/*.blend", "**/*.blend1")
}

sourceSets.main {
    resources { // include generated resources in resources
        srcDir("src/generated/resources")
        exclude("src/generated/resources/.cache")
    }
    blossom.javaSources {
        property("version", "mod_version"())
        property("gitCommit", rootProject.extra["gitHash"].toString())
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
