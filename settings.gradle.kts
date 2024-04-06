pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.quiltmc.org/repository/release")
        gradlePluginPortal()
    }
}

include("common")
include("fabric")
include("forge")

rootProject.name = "create-numismatics"
