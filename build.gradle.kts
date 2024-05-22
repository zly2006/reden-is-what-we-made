@file:Suppress("PropertyName")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.incremental.createDirectory
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
val imgui_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"

    id("fabric-loom") version "1.6-SNAPSHOT"
    `maven-publish`
    id("org.ajoberstar.grgit") version "5.2.2"
    id("com.redenmc.yamlang") version "1.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val artifactType = Attribute.of("artifactType", String::class.java)
val publized = Attribute.of("publized", Boolean::class.javaObjectType)
dependencies {
    attributesSchema {
        attribute(publized)
    }
    artifactTypes.getByName("jar") {
        attributes.attribute(publized, false)
    }
}

configurations.getByName("include") { // only "include" artifacts are "ourselves", so just make them all public!
    allArtifacts.all {
        if (isCanBeResolved) {
            attributes.attribute(publized, true)
            println("configuration ${this.name} ${allDependencies.toList()}")
        }
    }
}

// todo: (gradle bug) incremental transformation
abstract class Publize : TransformAction<TransformParameters.None> {
    override fun getParameters() = error("No parameters needed")

    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputArtifact
    abstract val input: Provider<FileSystemLocation>

    @get:Inject
    abstract val exec: ExecOperations

    override fun transform(outputs: TransformOutputs) {
        val fileName = inputArtifact.get().asFile.name
        println("input: ${input.get().asFile.absolutePath} artifact: $fileName")
        val outputFile = outputs.file("$fileName.publized.jar")
        println("output: ${outputFile.absolutePath}")
//        println("AAAAAAA")
//        try {
//            val changedFiles = inputChanges.getFileChanges(input)
//                .filter { it.fileType == FileType.FILE }
//                .map { it.file to it.changeType }
//                .toMutableList()
//            println(changedFiles)
//            println("AAAAAAA" + outputFile.exists())
//            if (!outputFile.exists()) {
//                println("Output file does not exist, transforming...")
//                changedFiles.add(Pair(inputArtifact.get().asFile, ChangeType.ADDED))
//            }
//            changedFiles.forEach { (file, changeType) ->
//                when (changeType) {
//                    ChangeType.ADDED,
//                    ChangeType.MODIFIED -> {
//                        println("Processing file ${file.name}")

        val file = input.get().asFile
        outputFile.parentFile.mkdirs()

        this.exec.run {
            javaexec {
                classpath(File("classpath/public-jar-1.2-all.jar"))
                mainClass.set("Main")
                args(
                    file.absolutePath,
                    outputFile.absolutePath,
                    "--field"
                )
            }
            println("transformed.")
        }

        println("OK, ${file.absolutePath} -> ${outputFile.absolutePath}")
//        outputs.file(outputFile)
    }

//                    ChangeType.REMOVED  -> {
//                        println("Removing leftover output file ${outputFile.absolutePath}")
//                        outputFile.delete()
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
}

dependencies {
    registerTransform(Publize::class) {
        from.attribute(publized, false).attribute(artifactType, "jar")
        to.attribute(publized, true).attribute(artifactType, "jar")
    }
}

enum class VersionType(val prereleaseName: String) {
    RELEASE("stable"), BETA("beta"), DEV("dev")
}

val versionType = when (System.getenv()["REDEN_BUILD_TYPE"]) {
    "RELEASE" -> VersionType.RELEASE
    "BETA" -> VersionType.BETA
    else   -> VersionType.DEV
}
val gitBranch = grgit.branch?.current()?.name ?: "no-git"

version = buildString {
    val commitsCount = grgit.log()?.size?.toString()
    val gitHash = grgit.head()?.id?.substring(0, 7) ?: "nogit"
    val ciNumber =
        if (System.getenv()["GITHUB_ACTIONS"] == "true") "gh-ci-${System.getenv()["GITHUB_RUN_NUMBER"]}"
        else null
    append(mod_version) // major.minor
    if (commitsCount != null && versionType != VersionType.RELEASE) {
        append(".")
        append(commitsCount) // patch
    }

    append("-")
    append(versionType.prereleaseName) // pre: stable/beta/dev

    append("+") // build
    append(gitBranch) // branch, usually mc version
    append(".")
    append(gitHash)
    if (ciNumber != null) {
        append(".")
        append(ciNumber)
    }
}
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
            val buildTime = grgit.head()?.dateTime?.toEpochSecond()?.times(1000L) ?: System.currentTimeMillis()
            filesMatching("fabric.mod.json") {
                expand(
                    mapOf(
                        "version" to project.version,
                        "is_main_branch" to is_main_branch,
                        "build_timestamp" to buildTime,
                        "git_branch" to gitBranch,
                        "git_commit" to grgit.head()?.id,
                    )
                )
            }
        }

        test {
            workingDir = file("run").also { it.createDirectory() }
            useJUnitPlatform()
        }

        jar {
            from("LICENSE") {
                rename { "${it}_${base.archivesName.get()}" }
            }
        }

        shadowJar {
            isZip64 = true
            configurations = listOf(project.configurations.getByName("shadow"))
            archiveClassifier = "shadow-dev"
            dependencies {
                exclude(dependency("org.lwjgl:lwjgl"))
                exclude(dependency("org.lwjgl:lwjgl-glfw"))
                exclude(dependency("org.lwjgl:lwjgl-opengl"))

                // glfw is embedded in minecraft
                exclude("windows/x64/org/lwjgl/**")
                exclude("windows/x86/org/lwjgl/**")
                exclude("linux/x64/org/lwjgl/**")
                exclude("linux/x86/org/lwjgl/**")
                exclude("macos/x64/org/lwjgl/**")
                exclude("macos/arm64/org/lwjgl/**")
                exclude("org/lwjgl/**")
            }
        }

        remapJar {
            dependsOn(shadowJar)
            inputFile.set(shadowJar.get().archiveFile.get())
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
afterEvaluate {
    loom.runs.configureEach {// https://fabricmc.net/wiki/tutorial:mixin_hotswaps
//        vmArg("-javaagent:${ configurations.compileClasspath.find { it.name.contains("sponge-mixin") } }")

        vmArg("-Dmixin.debug.export=true")
    }
}

tasks.create<ShadowJar>("exportJat") {
    /**
     * Note: use of this shadowJar task is on your own risk.
     * It contains minecraft classes so make sure to not distribute it.
     * See Mojang's EULA for more information.
     */

    isZip64 = true
    exclude("META-INF/**")
    exclude("_COROUTINE/**")
    exclude("kotlin/**")
    exclude("kotlinx/**")
    exclude("macos/**")
    exclude("windows/**")
    exclude("linux/**")
    exclude("io/netty/**")
    exclude("com/ibm/**")
    exclude("assets/minecraft/textures/**")
    exclude("*.json")
    exclude("*.jpg")
    exclude("*.png")
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
            classpath(File("classpath/public-jar-1.2-all.jar"))
            mainClass.set("Main")
            args(
                jar.absolutePath,
                jar.toPath().parent.resolve("publiced-" + jar.name).absolutePathString(),
                "--field"
            )
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loader_version}")


    implementation("com.google.code.findbugs:jsr305:3.0.2")

//    implementation("io.github.spair:imgui-java-binding:${imgui_version}")
//    shadow("io.github.spair:imgui-java-binding:${imgui_version}")
//    implementation("io.github.spair:imgui-java-lwjgl3:${imgui_version}")
//    shadow("io.github.spair:imgui-java-lwjgl3:${imgui_version}")
//
//    implementation("io.github.spair:imgui-java-natives-windows:${imgui_version}")
//    shadow("io.github.spair:imgui-java-natives-windows:${imgui_version}")
//    implementation("io.github.spair:imgui-java-natives-linux:${imgui_version}")
//    shadow("io.github.spair:imgui-java-natives-linux:${imgui_version}")
//    implementation("io.github.spair:imgui-java-natives-macos:${imgui_version}")
//    shadow("io.github.spair:imgui-java-natives-macos:${imgui_version}")

    // ImGui, https://github.com/SpaiR/imgui-java/pull/190
    val imguiAll = "imgui-app-1.86.11-11-gbdf3fc2-all.jar"
    implementation(files("classpath/$imguiAll"))
    shadow(files("classpath/$imguiAll"))

    // Essential dependencies
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.11.0+kotlin.2.0.0")
    modImplementation("fi.dy.masa.malilib:malilib-fabric-1.20.4:0.18.0")
    modImplementation("carpet:fabric-carpet:1.20.3-1.4.128+v231205")
    modImplementation("io.wispforest:owo-lib:${owo_version}")
    // Game test
    modImplementation("net.fabricmc:fabric-loader-junit:${loader_version}")
    // Embedded dependencies
    include(implementation("com.redenmc:brigadier-kotlin-dsl:1.0-SNAPSHOT")!!)
    include(implementation("com.squareup.okhttp3:okhttp:4.11.0")!!)
    include(implementation("org.eclipse.jgit:org.eclipse.jgit")!!)
    constraints {
        implementation("org.eclipse.jgit:org.eclipse.jgit:${jgit_version}").run {
            attributes {
                attribute(publized, true)
            }
            version { require(jgit_version) }
        }
    }
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

task("getVersion") {
    // generate .reden-version in build/ folder
    doLast {
        file("build/.reden-version").writeText(project.version as String)
        file("build/.reden-short-version").writeText(buildString {
            val commitsCount = grgit.log()?.size?.toString()
            append(mod_version) // major.minor
            if (commitsCount != null && versionType != VersionType.RELEASE) {
                append(".")
                append(commitsCount)
            }
            append("-")
            append(versionType.prereleaseName)
            append("+")
            append(gitBranch)
        })
    }
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
            url = uri(System.getenv()["MAVEN_URK"] ?: "https://maven.starlight.cool/artifactory/reden")
            credentials {
                username = System.getenv()["MAVEN_USER"]
                password = System.getenv()["MAVEN_PASSWORD"]
            }
        }
    }
}
