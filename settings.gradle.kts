pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Reden"
            url = uri("https://maven.starlight.cool/artifactory/reden")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
