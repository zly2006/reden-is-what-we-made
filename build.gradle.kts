import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlin.io.path.absolutePathString
import kotlin.math.floor

val maven_group: String by project
val mod_version: String by project
val is_main_branch: String by project
val archives_base_name: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val jgit_version: String by project
val fabric_version: String by project
val owo_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("fabric-loom") version "1.5-SNAPSHOT"
    `maven-publish`
    id("org.ajoberstar.grgit") version "5.2.2"
    id("com.redenmc.yamlang") version "1.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

ext {
    val versionType: String = when (System.getenv()["REDEN_BUILD_TYPE"]) {
        "RELEASE" -> "stable"
        "BETA" -> "beta"
        else -> "dev"
    }
    val commitsCount = grgit.log()?.size?.toString()
    val gitBranch = grgit.branch?.current()?.name ?: "no-git"
    val gitHash = grgit.head()?.id?.substring(0, 7) ?: "nogit"
    val ciNumber =
        if (System.getenv()["GITHUB_ACTIONS"] == "true") "gh-ci-${System.getenv()["GITHUB_RUN_NUMBER"]}"
        else null

    set("mod_version", buildString {
        append(mod_version) // major.minor
        if (commitsCount != null && versionType != "stable") {
            append(".")
            append(commitsCount) // patch
        }

        append("-")
        append(versionType) // pre: stable/beta/dev

        append("+") // build
        append(gitBranch) // branch
        append(".")
        append(gitHash)
        if (ciNumber != null) {
            append(".")
            append(ciNumber)
        }
    })
}

setVersion(ext.get("mod_version")!!)
group = maven_group

allprojects {
    repositories {
        mavenCentral()
        maven {
            name = "Reden"
            url = uri("https://maven.starlight.cool/artifactory/reden")
        }
        maven {
            name = "Masa Maven"
            url = uri("https://masa.dy.fi/maven")
        }
        maven { url = uri("https://maven.wispforest.io") }
        maven { url = uri("https://maven.terraformersmc.com/releases/") }
        maven {
            name = "CurseForge"
            url = uri("https://cursemaven.com")
        }
        maven {
            name = "Modrinth"
            url = uri("https://api.modrinth.com/maven")
        }
        maven {
            name = "CottonMC"
            url = uri("https://server.bbkr.space/artifactory/libs-release")
        }
    }

    tasks {
        processResources {
            inputs.property("version", project.version)
            filesMatching("fabric.mod.json") {
                expand(
                    mapOf(
                        "version" to project.version,
                        "is_main_branch" to is_main_branch,
                        "build_timestamp" to System.currentTimeMillis(),
                        "git_branch" to grgit.branch?.current()?.name,
                        "git_commit" to grgit.head()?.id,
                    )
                )
            }
        }

        test {
            useJUnitPlatform()
        }

        jar {
            from("LICENSE") {
                rename { "${it}_${base.archivesName.get()}" }
            }
        }
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xjvm-default=all")
        }
        jvmToolchain(17)
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")

    // Essential dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.10.17+kotlin.1.9.22")
    modImplementation("fi.dy.masa.malilib:malilib-fabric-1.20.4:0.18.0")
    modImplementation("carpet:fabric-carpet:1.20.3-1.4.128+v231205")
    modImplementation("io.wispforest:owo-lib:${owo_version}")
    // Game test
    modImplementation("net.fabricmc:fabric-loader-junit:${loader_version}")
    // Embedded dependencies
    include(implementation("com.squareup.okhttp3:okhttp:4.11.0")!!)
    include(implementation("org.eclipse.jgit:org.eclipse.jgit:${jgit_version}")!!)
    include(implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:${jgit_version}")!!)
    include(implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.apache:${jgit_version}")!!)
    include(implementation("com.squareup.okio:okio-jvm:3.2.0")!!)
    include(implementation("com.jcraft:jsch:0.1.55")!!)

    // Optional dependencies
    modImplementation("fi.dy.masa.litematica:litematica:0.17.1")
    modImplementation("fi.dy.masa.tweakeroo:tweakeroo:0.19.2")
    modImplementation("com.glisco:isometric-renders:0.4.7+1.20.3")
    modImplementation("com.terraformersmc:modmenu:9.0.0")

    // Runtime only dependencies (game optimization)
    modRuntimeOnly("maven.modrinth:ferrite-core:6.0.3-fabric")
    modRuntimeOnly("maven.modrinth:in-game-account-switcher:8.0.2-fabric1.20.4")
    modRuntimeOnly("maven.modrinth:notenoughcrashes:4.4.7+1.20.4-fabric")
}

base {
    archivesName = archives_base_name
}

loom {
    accessWidenerPath = file("src/main/resources/reden.accesswidener")
}

yamlang {
    targetSourceSets = listOf(sourceSets.main.get())
    inputDir = "assets/reden/lang"
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        maven {
            url = uri("https://maven.starlight.cool/artifactory/reden")
            credentials {
                username = System.getenv()["MAVEN_USER"]
                password = System.getenv()["MAVEN_PASSWORD"]
            }
        }
    }
}

tasks.withType<ShadowJar> {
    /**
     * Note: use of this shadowJar task is on your own risk.
     * It contains minecraft classes so make sure to not distribute it.
     * See Mojang's EULA for more information.
     */
    isZip64 = true
    exclude("META-INF/**")
    exclude("kotlin/**")
    exclude("kotlinx/**")
    exclude("*.json")
    exclude("*.properties")
    exclude("*.accesswidener")
    exclude("LICENSE*")
    exclude("Log4j*")
    exclude("mixin/**")
    exclude("mappings/**")

    doLast {
        val jar = archiveFile.get().asFile
        println("Jar size: " + floor(jar.length().toDouble() / 1024 / 1024) + "MB")

        javaexec {
            classpath(files("classpath/public-jar-1.0-SNAPSHOT-all.jar"))
            mainClass.set("com.redenmc.publicizer.MainKt")
            args(
                jar.absolutePath,
                jar.toPath().parent.resolve("publiced-" + jar.name).absolutePathString()
            )
        }
    }
}
