pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/")
        gradlePluginPortal()
    }
}

include("common")
//include("fabric")
include("neoforge")

rootProject.name = "create-numismatics"
