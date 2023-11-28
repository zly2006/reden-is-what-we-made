package com.github.zly2006.reden.update

import com.github.zly2006.reden.Reden
import net.fabricmc.loader.api.FabricLoader.getInstance
import net.fabricmc.loader.impl.gui.FabricGuiEntry
import net.fabricmc.loader.impl.util.LoaderUtil
import net.fabricmc.loader.impl.util.UrlUtil
import java.io.DataOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

val customValues = getInstance().getModContainer(Reden.MOD_ID).get().metadata.getCustomValue("reden").asObject
val shouldCheck = Reden.MOD_VERSION.friendlyString.contains("dev")
        && customValues["is_main_branch"].asString.toBoolean()

val debugAlwaysRestart = true

fun checkUpdate() {
    if (shouldCheck) {

    }
}

fun relaunch() {
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

    if (javaPath == null) throw RuntimeException("can't find java executable in $javaBinDir")

    val process = ProcessBuilder(
        javaPath.toString(), "-Xmx100M", "-cp", UrlUtil.LOADER_CODE_SOURCE.toString(),
        FabricGuiEntry::class.java.getName()
    )
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()

    val shutdownHook = Thread { process.destroy() }

    Runtime.getRuntime().addShutdownHook(shutdownHook)

    DataOutputStream(process.outputStream)

    val rVal = process.waitFor()

    Runtime.getRuntime().removeShutdownHook(shutdownHook)

    if (rVal != 0) throw IOException("subprocess exited with code $rVal")
}
