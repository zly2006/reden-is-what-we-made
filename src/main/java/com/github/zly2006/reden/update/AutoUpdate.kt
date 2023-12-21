package com.github.zly2006.reden.update

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import net.fabricmc.loader.api.FabricLoader.getInstance
import net.fabricmc.loader.impl.util.LoaderUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Util
import net.minecraft.util.Util.OperatingSystem
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.*
import kotlin.random.Random.Default.nextInt

val customValues = getInstance().getModContainer(Reden.MOD_ID).get().metadata.getCustomValue("reden").asObject
val shouldCheck = Reden.MOD_VERSION.friendlyString.contains("dev")
        && customValues["is_main_branch"].asString.toBoolean()

val debugAlwaysRestart = true

fun checkUpdate() {
    if (shouldCheck) {

    }
}

fun relaunch(newJar: Path?) {
    val javaBinDir = LoaderUtil.normalizePath(Paths.get(System.getProperty("java.home"), "bin"))
    val executables = arrayOf("javaw.exe", "java.exe", "java")
    var javaPath: Path? = null

    for (executable in executables) {
        val path = javaBinDir.resolve(executable)
        if (Files.isRegularFile(path)) {
            javaPath = path
            break
        }
    }
    if (javaPath == null) error("can't find java executable in $javaBinDir")

    val cmd = ProcessHandle.current().info().cmd()
    val currentJar = getInstance().getModContainer(Reden.MOD_ID).get().origin.paths.firstOrNull { it.extension == "jar" }
    println(currentJar)

    println(cmd)
    println(Path("").absolutePathString())
    Path(".cache", "reden").createDirectories()
    @Suppress("NAME_SHADOWING")
    val newJar = newJar ?: if (currentJar != null) {
        val path = Path(".cache", "reden", "reden.jar")
        Files.copy(currentJar, path, StandardCopyOption.REPLACE_EXISTING)
        path
    } else null
    val currentJarP = currentJar?.absolutePathString()?.quoted() ?: "null"
    val newJarP = newJar?.absolutePathString()?.quoted() ?: "null"
    println(currentJarP)
    println(newJarP)
    if (Util.getOperatingSystem() != OperatingSystem.WINDOWS) {
        Path(".cache", "reden", "relaunch.sh").writeText(
            """
#!/bin/sh
sleep 1
cd ${Paths.get("").absolutePathString().quoted()} || exit
${
    if (newJar != null) {
        "echo Replacing $currentJarP with $newJarP... >> out.log\n" +
                "rm ${currentJarP}\n" +
                "cp $newJarP $currentJarP-${nextInt(10)}\n"
    } else "echo No new jar found. >> out.log\n"
}
echo "Relaunching..." >> out.log
$cmd >> out.log 2>&1
echo "Exited with code $?" >> out.log
            """.trimIndent()
        )

        val process = ProcessBuilder(
            "/bin/sh", "-c", "chmod +x .cache/reden/relaunch.sh && .cache/reden/relaunch.sh"
        ).start()
    }
    else {
        Path(".cache", "reden", "relaunch.bat").writeText(
            """
@echo off
timeout /t 1
cd ${Paths.get("").absolutePathString().quoted()}
${
    if (newJar != null) {
        "echo Replacing $currentJarP with $newJarP... >> out.log\n" +
                "del $currentJarP\n" +
                "copy $newJarP $currentJarP-${nextInt(10)}\n"
    } else "echo No new jar found. >> out.log\n"
}
echo Relaunching... >> out.log
$cmd >> out.log
echo Exited with code %ERRORLEVEL% >> out.log
            """.trimIndent()
        )

        val process = ProcessBuilder(
            "cmd", "/c", "start .cache/reden/relaunch.bat"
        ).start()
    }

    Reden.LOGGER.info("Waiting to restart...")
    if (isClient) {
        MinecraftClient.getInstance().stop()
    } else {
        server.stop(true)
    }
}

fun String.quoted(): String {
    return "\"${this.replace("\"", "\\\"").replace("$", "\\$")}\""
}

private fun ProcessHandle.Info.cmd(): String {
    val cmd = command().orElseThrow().quoted()
    val args = arguments().orElse(emptyArray())
    println("command: $cmd")
    println("args: ${args.joinToString(" ")}")
    println("cmdline: ${commandLine().orElse("null")}")
    args.forEach { println(it) }
    val cp = if ("-cp" !in args && "-classpath" !in args) {
        "-cp " + System.getProperty("java.class.path").split(File.pathSeparator)
            .joinToString(File.pathSeparator) { it.quoted() }
    } else ""
    return "$cmd $cp ${args.filter { "/private/var" !in it } // MacOS patch
        .joinToString(" ") { it.quoted() }}"
}
