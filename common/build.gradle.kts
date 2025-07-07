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
    //modCompileOnly("com.simibubi.create:create-fabric-${"minecraft_version"()}:${"create_fabric_version"()}")

    // Create and its dependencies
    modImplementation("com.simibubi.create:create-${"minecraft_version"()}:${"create_neoforge_version"()}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:Ponder-NeoForge-${"minecraft_version"()}:${"ponder_version"()}")
    modImplementation("com.tterrag.registrate:Registrate:${"registrate_neoforge_version"()}")
    modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${"minecraft_version"()}:${"flywheel_version"()}")
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${"minecraft_version"()}:${"flywheel_version"()}")
    
    // Needed for compiling
    compileOnly("net.neoforged:neoforge:${"neoforge_version"()}")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // required for proper remapping and compiling
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${"fabric_api_version"()}")

    // Carry On
    //modCompileOnly("tschipp.carryon:carryon-fabric-${"minecraft_version"()}:${"carryon_fabric_version"()}")
    modCompileOnly("tschipp.carryon:carryon-neoforge-${"minecraft_version"()}:${"carryon_neoforge_version"()}")
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
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}
