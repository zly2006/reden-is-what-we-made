pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.4.4"
}

rootProject.name = "Reden"
include("common")

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    shared {
        versions(
//            "1.20.4",
            "1.21.1",
//            "1.21.4"
        )
    }
    create(rootProject)
}
