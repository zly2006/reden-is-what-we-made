plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.9.2" apply false

    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.serialization") version "2.0.0" apply false
    id("io.github.goooler.shadow") version "8.1.7" apply false
    base
    id("dev.kikugie.j52j") version "1.0.2" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.8.4" apply false
    id("org.ajoberstar.grgit") version "5.0.0-rc.3"
}
stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://maven.shedaniel.me/")
        }
        maven {
            url = uri("https://jitpack.io/")
        }
        maven {
            url = uri("https://masa.dy.fi/maven")
        }
    }

    base {
        archivesName = property("mod.id") as String + "-" + name
    }
}

// Builds every version into `build/libs/{mod.version}/`
stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")

    dependsOn(project(":cli").tasks.named("shadowJar"))
}

/*
// Publishes every version
stonecutter registerChiseled tasks.register("chiseledPublishMods", stonecutter.chiseled) {
    group = "project"
    ofTask("publishMods")
}
*/

stonecutter configureEach {
    swap("mod_version", "\"${property("mod.version")}\"")
    swap("git_commit", "\"${grgit.head().abbreviatedId}\"")
    swap("commit_date", "\"${grgit.head().dateTime.toString().substringBefore("[")}\"")
    dependency("fapi", project.property("deps.fabric_api").toString())
}
