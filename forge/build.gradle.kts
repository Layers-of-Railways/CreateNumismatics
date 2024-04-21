import dev.ithundxr.silk.ChangelogText

architectury.forge()

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath

    forge {
        mixinConfig("numismatics-common.mixins.json")
        mixinConfig("numismatics.mixins.json")

        convertAccessWideners = true
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
    }
}

repositories {
    // mavens for Forge-exclusives
    maven("https://maven.theillusivec4.top/") // Curios
    maven("https://maven.terraformersmc.com/releases/") // EMI
    maven("https://jitpack.io/") // Mixin Extras, Fabric ASM
    maven("https://maven.tterrag.com/") { // Create Forge and Registrate Forge
        content {
            includeGroup("com.tterrag.registrate")
            includeGroup("com.simibubi.create")
        }
    }
    maven("https://squiddev.cc/maven/") { // CC Tweaked
        content {
            includeGroup("cc.tweaked")
        }
    }
}

dependencies {
    forge("net.minecraftforge:forge:${"minecraft_version"()}-${"forge_version"()}")
    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionForge")) { isTransitive = false }

    // Create and its dependencies
    modImplementation("com.simibubi.create:create-${"minecraft_version"()}:${"create_forge_version"()}:slim") { isTransitive = false }
    modImplementation("com.tterrag.registrate:Registrate:${"registrate_forge_version"()}")
    modImplementation("com.jozufozu.flywheel:flywheel-forge-${"flywheel_forge_minecraft_version"()}:${"flywheel_forge_version"()}")

    modLocalRuntime("dev.emi:emi-forge:${"emi_version"()}")

    modCompileOnly("cc.tweaked:cc-tweaked-${"minecraft_version"()}-forge-api:${"cc_version"()}")
    modCompileOnly("cc.tweaked:cc-tweaked-${"minecraft_version"()}-core-api:${"cc_version"()}")

    forgeRuntimeLibrary("cc.tweaked:cobalt:0.9.3")
    forgeRuntimeLibrary("com.jcraft:jzlib:1.1.3")
    forgeRuntimeLibrary("io.netty:netty-codec-http:4.1.82.Final")
    forgeRuntimeLibrary("io.netty:netty-codec-socks:4.1.82.Final")
    forgeRuntimeLibrary("io.netty:netty-handler-proxy:4.1.82.Final")

    if ("enable_cc"().toBoolean()) {
        modLocalRuntime("cc.tweaked:cc-tweaked-${"minecraft_version"()}-forge:${"cc_version"()}")
    }


    // Carry On
    modCompileOnly("tschipp.carryon:carryon-forge-${"minecraft_version"()}:${"carryon_forge_version"()}")
    if ("enable_carryon"().toBoolean()) {
        modLocalRuntime("tschipp.carryon:carryon-forge-${"minecraft_version"()}:${"carryon_forge_version"()}")
    }

    compileOnly("io.github.llamalad7:mixinextras-common:${"mixin_extras_version"()}")
    include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-forge:${"mixin_extras_version"()}")!!)!!)
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
