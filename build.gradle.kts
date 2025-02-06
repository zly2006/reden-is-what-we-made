plugins {
    `maven-publish`
    id("fabric-loom")
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.github.goooler.shadow") version "8.1.7"
    id("dev.kikugie.j52j")
    id("org.ajoberstar.grgit")
    id("me.modmuss50.mod-publish-plugin")
}

class ModData {
    val id = property("mod.id").toString()
    val name = property("mod.name").toString()
    val version = property("mod.version").toString()
    val group = property("mod.group").toString()
}

class ModDependencies {
    operator fun get(name: String) = property("deps.$name").toString()
}

val mod = ModData()
val deps = ModDependencies()
val mcVersion = stonecutter.current.version
val mcDep = property("mod.mc_dep").toString()

version = "${mod.version}+$mcVersion"
group = mod.group
base { archivesName.set(mod.id) }

loom {
    accessWidenerPath = rootProject.file("src/main/resources/reden.accesswidener")
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    mavenCentral()
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    maven {
        name = "Masa Maven"
        url = uri("https://masa.dy.fi/maven")
    }
    maven { url = uri("https://maven.wispforest.io") }
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
    maven {
        name = "CottonMC"
        url = uri("https://server.bbkr.space/artifactory/libs-release")
    }
    maven { url = uri("https://jitpack.io") }
    maven("https://maven.creeperhost.net")
}

dependencies {
    fun fapi(vararg modules: String) = modules.forEach {
        modImplementation(fabricApi.module(it, deps["fabric_api"]))
    }

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.6.10")

    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$mcVersion+build.${deps["yarn_build"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${deps["fabric_loader"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${deps["kotlin_loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps["fabric_api"]}")

    // ImGui, https://github.com/SpaiR/imgui-java/pull/190
//    val imguiAll = "imgui-app-1.86.11-11-gbdf3fc2-all.jar"
//    implementation(files("classpath/$imguiAll"))
//    shadow(files("classpath/$imguiAll"))

    // Essential dependencies
    modImplementation("carpet:fabric-carpet:${deps["carpet"]}")
    modImplementation("io.wispforest:owo-lib:${deps["owo_version"]}")
    modImplementation("com.github.sakura-ryoko:malilib:b012771deb")
    // Game test
    modImplementation("net.fabricmc:fabric-loader-junit:${deps["fabric_loader"]}")
    // Embedded dependencies
    include(implementation("com.squareup.okio:okio-jvm:3.2.0")!!)
    include(implementation("com.squareup.okhttp3:okhttp:4.11.0")!!)
    include(implementation("org.sejda.imageio:webp-imageio:0.1.6")!!)
    // Optional dependencies
    modImplementation("com.github.sakura-ryoko:litematica:8e285513a6")
    modImplementation("com.github.sakura-ryoko:tweakeroo:36a640f2c6")
//    modImplementation("com.glisco:isometric-renders:0.4.7+1.20.3")
    modImplementation("com.terraformersmc:modmenu:11.0.1")

    // Runtime only dependencies (game optimization)
    modRuntimeOnly("maven.modrinth:ferrite-core:7.0.1-fabric")
//    modRuntimeOnly("maven.modrinth:notenoughcrashes:4.4.7+1.20.4-fabric")
}

loom {
    decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"
    }
}

val javaVersion =
    if (stonecutter.eval(mcVersion, ">=1.20.6")) 21
    else 17

java {
    withSourcesJar()
    targetCompatibility = JavaVersion.toVersion(javaVersion)
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
}

kotlin {
    jvmToolchain(javaVersion)
}

tasks.processResources {
    inputs.property("id", mod.id)
    inputs.property("name", mod.name)
    inputs.property("version", mod.version)
    inputs.property("mcdep", mcDep)

    val buildTime = grgit.head()?.dateTime?.toEpochSecond()?.times(1000L) ?: System.currentTimeMillis()
    val map = mapOf(
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "mcdep" to mcDep,
        "build_timestamp" to buildTime,
    )

    filesMatching("fabric.mod.json") { expand(map) }

    dependsOn(project(":common").tasks.processResources)
    outputs.upToDateWhen { false }
    doLast {
        // copying this is for dev only, int here is a shadowJar task
        copy {
            from(project(":common").tasks.processResources.get().outputs.files)
            into(outputs.files.first())
        }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}

publishMods {
    file = tasks.remapJar.get().archiveFile
    displayName = "${mod.name} ${mod.version} for $mcVersion"
    version = "${mod.version}+$mcVersion"
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

//    dryRun = providers.environmentVariable("MODRINTH_TOKEN")
//        .getOrNull() == null || providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

//    modrinth {
//        projectId = property("publish.modrinth").toString()
//        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
//        minecraftVersions.addAll(
//            property("mod.mc_targets").toString().split(" ")
//                .filter { it.isNotBlank() }
//                .plus(mcVersion)
//                .distinct()
//        )
//        requires("fabric-api", "fabric-language-kotlin")
//        optional("polylib")
//    }

//    curseforge {
//        projectId = property("publish.curseforge").toString()
//        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
//        minecraftVersions.addAll(
//            property("mod.mc_targets").toString().split(" ")
//                .filter { it.isNotBlank() }
//                .plus(mcVersion)
//                .distinct()
//        )
//        requires("fabric-api", "fabric-language-kotlin")
//        optional("polylib")
//    }
}
