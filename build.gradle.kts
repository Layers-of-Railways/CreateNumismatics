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

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.configurationcache.extensions.capitalized
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.CheckClassAdapter
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

plugins {
    java
    `maven-publish`
    id("architectury-plugin") version "3.4.+"
    id("dev.architectury.loom") version "1.6.+" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.3.4" apply false // https://github.com/modmuss50/mod-publish-plugin
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
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

    val capitalizedName = project.name.capitalized()

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

    repositories {
        mavenCentral()
        maven("https://maven.shedaniel.me/") // Cloth Config, REI
        maven("https://maven.blamejared.com/") // JEI, Carry On
        maven("https://maven.parchmentmc.org") // Parchment mappings
        maven("https://maven.quiltmc.org/repository/release") // Quilt Mappings
        maven("https://maven.tterrag.com/") { // Flywheel
            content {
                // need to be specific here due to version overlaps
                includeGroup("com.jozufozu.flywheel")
            }
        }
    }

    @Suppress("UnstableApiUsage")
    dependencies {
        "minecraft"("com.mojang:minecraft:${"minecraft_version"()}")
        // layered mappings - Mojmap names, parchment and QM docs and parameters
        "mappings"(loom.layered {
            mappings("org.quiltmc:quilt-mappings:${"minecraft_version"()}+build.${"qm_version"()}:intermediary-v2")
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
                    url = uri("https://maven.ithundxr.dev/${maven}")
                    credentials {
                        username = "numismatics-github"
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

    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "me.modmuss50.mod-publish-plugin")

    architectury {
        platformSetupLoomIde()
    }

    tasks.named<RemapJarTask>("remapJar") {
        val shadowJar = project.tasks.named<ShadowJar>("shadowJar").get()
        inputFile.set(shadowJar.archiveFile)
        injectAccessWidener = true
        dependsOn(shadowJar)
        archiveClassifier = null
        doLast {
            transformJar(project.path, outputs.files.singleFile)
        }
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
        // set up properties for filling into metadata
        val properties = mapOf(
            "version" to version,
            "minecraft_version" to "minecraft_version"(),
            "fabric_api_version" to "fabric_api_version"(),
            "fabric_loader_version" to "fabric_loader_version"(),
            "forge_version" to "forge_version"().split(".")[0], // only specify major version of forge
            "create_forge_version" to "create_forge_version"().split("-")[0], // cut off build number
            "create_fabric_version" to "create_fabric_version"().split("+")[0] // Trim +mcX.XX.X from version string
        )

        inputs.properties(properties)

        filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml")) {
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
}

fun transformJar(projectPath: String, jar: File) {
    val contents = linkedMapOf<String, ByteArray>()
    JarFile(jar).use {
        it.entries().asIterator().forEach { entry ->
            if (!entry.isDirectory) {
                contents[entry.name] = it.getInputStream(entry).readAllBytes()
            }
        }
    }

    jar.delete()

    JarOutputStream(jar.outputStream()).use { out ->
        out.setLevel(Deflater.BEST_COMPRESSION)
        contents.forEach { var (name, data) = it
            if (name.startsWith("architectury_inject_${"archives_base_name"().lowercase(Locale.ROOT)}_common"))
                return@forEach

            if (name.endsWith(".json") || name.endsWith(".mcmeta")) {
                data = (JsonOutput.toJson(JsonSlurper().parse(data)).toByteArray())
            } else if (name.endsWith(".class")) {
                data = transformClass(projectPath, data)
            }

            out.putNextEntry(JarEntry(name))
            out.write(data)
            out.closeEntry()
        }
        out.finish()
        out.close()
    }
}

@Suppress("LocalVariableName")
fun transformClass(projectPath: String, bytes: ByteArray): ByteArray {
    val node = ClassNode()
    ClassReader(bytes).accept(node, 0)

    if (node.invisibleAnnotations != null ) {
        // Cache the field, so we don't CME the list during the remove
        val annotationNodes = node.invisibleAnnotations.toList()
        for (annotationNode in annotationNodes) {
            if (annotationNode.desc.equals("Ldev/ithundxr/createnumismatics/annotation/asm/CCForgeImpl;")) {
                // Remove
                node.invisibleAnnotations.remove(annotationNode)

                if (projectPath == ":forge") {
                    // Add the interface that's needed
                    node.interfaces.add("net/minecraftforge/common/capabilities/ICapabilityProvider")

                    // getCapability method
                    run {
                        val mv = node.visitMethod(
                            Opcodes.ACC_PUBLIC,
                            "getCapability",
                            "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;",
                            null,
                            null
                        )
                        mv.visitCode()

                        val L1 = Label()

                        // L0
                        mv.visitLabel(Label())
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitFieldInsn(
                            Opcodes.GETFIELD,
                            node.name,
                            "computerBehaviour",
                            "Lcom/simibubi/create/compat/computercraft/AbstractComputerBehaviour;"
                        )
                        mv.visitVarInsn(Opcodes.ALOAD, 1)
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "com/simibubi/create/compat/computercraft/AbstractComputerBehaviour",
                            "isPeripheralCap",
                            "(Lnet/minecraftforge/common/capabilities/Capability;)Z",
                            false
                        )
                        mv.visitJumpInsn(Opcodes.IFEQ, L1)

                        // L2
                        mv.visitLabel(Label())
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitFieldInsn(
                            Opcodes.GETFIELD,
                            node.name,
                            "computerBehaviour",
                            "Lcom/simibubi/create/compat/computercraft/AbstractComputerBehaviour;"
                        )
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "com/simibubi/create/compat/computercraft/AbstractComputerBehaviour",
                            "getPeripheralCapability",
                            "()Lnet/minecraftforge/common/util/LazyOptional;",
                            false
                        )
                        mv.visitInsn(Opcodes.ARETURN)

                        // L1
                        mv.visitLabel(L1)
                        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitVarInsn(Opcodes.ALOAD, 1)
                        mv.visitVarInsn(Opcodes.ALOAD, 2)
                        mv.visitMethodInsn(
                            Opcodes.INVOKESPECIAL,
                            node.name,
                            "getCapability",
                            "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;",
                            false
                        )
                        mv.visitInsn(Opcodes.ARETURN)

                        mv.visitEnd()
                    }

                    // invalidateCaps method
                    run {
                        val mv = node.visitMethod(Opcodes.ACC_PUBLIC, "invalidateCaps", "()V", null, null)
                        mv.visitCode()

                        // L0
                        val L0 = Label()
                        mv.visitLabel(L0)
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, node.name, "invalidateCaps", "()V", false)

                        // L1
                        mv.visitLabel(Label())
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitFieldInsn(
                            Opcodes.GETFIELD,
                            node.name,
                            "computerBehaviour",
                            "Lcom/simibubi/create/compat/computercraft/AbstractComputerBehaviour;"
                        )
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "com/simibubi/create/compat/computercraft/AbstractComputerBehaviour",
                            "removePeripheral",
                            "()V",
                            false
                        )

                        // L2
                        mv.visitLabel(Label())
                        mv.visitInsn(Opcodes.RETURN)

                        mv.visitEnd()
                    }
                }
            }
        }
    }

    val byteArray = ClassWriter(0).also { node.accept(it) }.toByteArray()

    // Verify the bytecode is valid
    ClassReader(byteArray).accept(CheckClassAdapter(null), 0)

    return byteArray
}

fun calculateGitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

fun hasUnstaged(): Boolean {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "status", "--porcelain")
        standardOutput = stdout
    }
    val result = stdout.toString().replace("M gradlew", "").trimEnd()
    if (result.isNotEmpty())
        println("Found stageable results:\n${result}\n")
    return result.isNotEmpty()
}

tasks.create("numismaticsPublish") {
    when (val platform = System.getenv("PLATFORM")) {
        "both" -> {
            dependsOn(tasks.build, ":fabric:publish", ":forge:publish", ":common:publish", ":fabric:publishMods", ":forge:publishMods")
        }
        "fabric", "forge" -> {
            dependsOn("${platform}:build", "${platform}:publish", "${platform}:publishMods")
        }
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
