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
            vmArg("-Dfabric-api.datagen.modid=railways")
            vmArg("-Dporting_lib.datagen.existing_resources=${common.file("src/main/resources")}")

            environmentVariable("DATAGEN", "TRUE")
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
    maven("https://mvn.devos.one/snapshots/") // Create Fabric, Porting Lib, Forge Tags, Milk Lib, Registrate Fabric
    maven("https://mvn.devos.one/releases") // Porting Lib Releases
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // Forge config api port
    maven("https://maven.cafeteria.dev/releases") // Fake Player API
    maven("https://maven.jamieswhiteshirt.com/libs-release") // Reach Entity Attributes
    maven("https://jitpack.io/") // Mixin Extras, Fabric ASM
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

    // because create fabric is a bit broken I think
    modImplementation("net.minecraftforge:forgeconfigapiport-fabric:4.2.11")

    // Development QOL
    modLocalRuntime("maven.modrinth:lazydfu:${"lazydfu_version"()}")
    modLocalRuntime("com.terraformersmc:modmenu:${"modmenu_version"()}")

    modLocalRuntime("dev.emi:emi-fabric:${"emi_version"()}")
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