pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven {
            name = "Reden"
            url = uri("https://maven.starlight.cool/artifactory/reden")
        }
    }
}
