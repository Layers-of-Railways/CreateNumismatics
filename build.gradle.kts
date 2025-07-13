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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.ithundxr.silk.ChangelogText
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

plugins {
    java
    `maven-publish`
    id("architectury-plugin") version "3.4.+"
    id("dev.architectury.loom") version "1.10.+" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.8.4" apply false // https://github.com/modmuss50/mod-publish-plugin
    id("com.gradleup.shadow") version "8.3.8" apply false
    id("dev.ithundxr.silk") version "0.11.+" // https://github.com/IThundxr/silk
}

val isRelease = System.getenv("RELEASE_BUILD")?.toBoolean() ?: false
val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toInt()
val gitHash = "\"${calculateGitHash() + (if (hasUnstaged()) "-modified" else "")}\""

architectury {
    minecraft = "minecraft_version"()
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    base.archivesName.set("archives_base_name"())
    group = "maven_group"()

    // Formats the mod version to include the loader, Minecraft version, and build number (if present)
    // example: 1.0.0+fabric-1.19.2-build.100 (or -local)
    val build = buildNumber?.let { "-build.${it}" } ?: "-local"

    version = "${"mod_version"()}+${project.name}-mc${"minecraft_version"() + if (isRelease) "" else build}"

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    java {
        withSourcesJar()
    }
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    
    setupRepositories()

    val capitalizedName = {
        if (project.name == "neoforge") {
            "NeoForge"
        } else {
            project.name.replaceFirstChar { it.titlecase() }
        }
    }();

    val loom = project.extensions.getByType<LoomGradleExtensionAPI>()
    loom.apply {
        silentMojangMappingsLicense()
        runs.configureEach {
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:+IgnoreUnrecognizedVMOptions")
            vmArg("-Dmixin.debug.export=true")
            vmArg("-Dmixin.env.remapRefMap=true")
            vmArg("-Dmixin.env.refMapRemappingFile=${projectDir}/build/createSrgToMcp/output.srg")
        }
    }

    @Suppress("UnstableApiUsage")
    dependencies {
        "minecraft"("com.mojang:minecraft:${"minecraft_version"()}")
        "mappings"(loom.layered {
            officialMojangMappings { nameSyntheticMembers = false }
            parchment("org.parchmentmc.data:parchment-${"minecraft_version"()}:${"parchment_version"()}@zip")
        })
    }

    publishing {
        publications {
            create<MavenPublication>("maven${capitalizedName}") {
                artifactId = "${"archives_base_name"()}-${project.name}-${"minecraft_version"()}"
                from(components["java"])
            }
        }

        repositories {
            val mavenToken = System.getenv("MAVEN_TOKEN")
            val maven = if (isRelease) "releases" else "snapshots"
            if (mavenToken != null && mavenToken.isNotEmpty()) {
                maven {
                    url = uri("https://mvn.devos.one/${maven}")
                    credentials {
                        username = "ithundxr-github"
                        password = mavenToken
                    }
                }
            }
        }
    }

    // from here down is platform configuration
    if(project.path == ":common") {
        return@subprojects
    }

    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    architectury {
        platformSetupLoomIde()
    }

    val remapJar = tasks.named<RemapJarTask>("remapJar") {
        val shadowJar = project.tasks.named<ShadowJar>("shadowJar").get()
        inputFile.set(shadowJar.archiveFile)
        injectAccessWidener = true
        dependsOn(shadowJar)
        archiveClassifier = null
    }

    val common: Configuration by configurations.creating
    val shadowCommon: Configuration by configurations.creating
    val development = configurations.maybeCreate("development${capitalizedName}")

    configurations {
        compileOnly.get().extendsFrom(common)
        runtimeOnly.get().extendsFrom(common)
        development.extendsFrom(common)
    }

    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier = "dev-shadow"
        configurations = listOf(shadowCommon)
        exclude("architectury.common.json")
        destinationDirectory = layout.buildDirectory.dir("devlibs").get()
    }

    tasks.processResources {
        val createNeoForgeVersion = "create_neoforge_version"().split("-")[0] // cut off build number
        val createNeoForgeUpperBounds = {
            val parts = createNeoForgeVersion.split(".").map { it.toInt() }
            val newMinor = parts[1] + 1
            "${parts[0]}.$newMinor.0"
        }()
        
        // set up properties for filling into metadata
        val properties = mapOf(
            "version" to version,
            "minecraft_version" to "minecraft_version"(),
            "fabric_api_version" to "fabric_api_version"(),
            "fabric_loader_version" to "fabric_loader_version"(),
            "neoforge_version" to "neoforge_version"(),
            "create_neoforge_version" to createNeoForgeVersion, 
            "create_neoforge_upper_bounds" to createNeoForgeUpperBounds,
            "create_fabric_version_range" to "create_fabric_version_range"()
        )

        inputs.properties(properties)

        filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
            expand(properties)
        }
    }

    tasks.jar {
        archiveClassifier = "dev"

        manifest {
            attributes(mapOf("Git-Hash" to gitHash))
        }
    }

    tasks.named<Jar>("sourcesJar") {
        val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })

        manifest {
            attributes(mapOf("Git-Hash" to gitHash))
        }
    }

    components.getByName<AdhocComponentWithVariants>("java") {
        withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
            skip()
        }
    }
    
    val releaseType = {
        val versionStr = version.toString()
        if (versionStr.contains("alpha")) {
            ReleaseType.ALPHA;
        } else if (versionStr.contains("beta")) {
            ReleaseType.BETA;
        } else {
            ReleaseType.STABLE;
        }
    }()
    configure<ModPublishExtension> {
        file.set(remapJar.get().archiveFile)
        version.set(project.version.toString())
        changelog = ChangelogText.getChangelogText(rootProject).toString()
        type = releaseType
        displayName = "Numismatics ${"mod_version"()} ${capitalizedName}} ${"minecraft_version"()}"
        modLoaders.add(project.name)
        
        val createVersionType = if (project.name == "fabric") "create-fabric" else "create"
        curseforge {
            projectId = "curseforge_id"()
            accessToken = System.getenv("CURSEFORGE_TOKEN")
            minecraftVersions.add("minecraft_version"())

            requires {
                slug = createVersionType
            }
        }

        modrinth {
            projectId = "modrinth_id"()
            accessToken = System.getenv("MODRINTH_TOKEN")
            minecraftVersions.add("minecraft_version"())

            requires {
                slug = createVersionType
            }
        }
    }
}

fun calculateGitHash(): String {
    try {
        val output = providers.exec {
            commandLine("git", "rev-parse", "HEAD")
        }
        return output.standardOutput.asText.get().trim()
    } catch (ignored: Throwable) {
        return "unknown"
    }
}

fun hasUnstaged(): Boolean {
    try {
        val output = providers.exec {
            commandLine("git", "status", "--porcelain")
        }
        val result = output.standardOutput.asText.get().replace("/M gradlew(\\.bat)?/", "").trim()
        if (result.isNotEmpty())
            println("Found stageable results:\n ${result}\n")
        return result.isNotEmpty()
    } catch (ignored: Throwable) {
        return false
    }
}

tasks.register("numismaticsPublish") {
    when (val platform = System.getenv("PLATFORM")) {
        "both" -> {
            dependsOn(tasks.build, ":fabric:publish", ":neoforge:publish", ":common:publish", ":fabric:publishMods", ":neoforge:publishMods")
        }
        "fabric", "neoforge" -> {
            dependsOn("${platform}:build", "${platform}:publish", "${platform}:publishMods")
        }
    }
}

fun Project.setupRepositories() {
    repositories {
        mavenCentral()
        exclusiveMaven("https://maven.parchmentmc.org", "org.parchmentmc.data") // Parchment mappings
        maven("https://maven.neoforged.net") // NeoForge
        exclusiveMaven( // Create, Ponder, Flywheel
            "https://maven.createmod.net",
            "com.simibubi.create",
            "net.createmod.ponder",
            "dev.engine-room.flywheel"
        )
        exclusiveMaven("https://maven.ithundxr.dev/snapshots", "com.tterrag.registrate")
        exclusiveMaven("https://maven.blamejared.com", "tschipp.carryon") // Carry On
        exclusiveMaven( // EMI, Mod Menu
            "https://maven.terraformersmc.com/releases", 
            "dev.emi", "com.terraformersmc.modmenu"
        )
        exclusiveMaven( // Forge config api port
            "https://raw.githubusercontent.com/Fuzss/modresources/main/maven",
            "fuzs.forgeconfigapiport"
        )
        
        //maven("https://maven.siphalor.de") // Amecs API (required by Carry On)
        //maven("https://maven.theillusivec4.top") // Curios
    }
}

fun RepositoryHandler.exclusiveMaven(url: String, vararg groups: String) {
    exclusiveContent {
        forRepository { maven(url) }
        filter {
            groups.forEach {
                includeGroup(it)
            }
        }
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
