package com.github.zly2006.reden.transformers.mapping

import com.github.zly2006.reden.report.httpClient
import net.fabricmc.mapping.tree.TinyTree
import okhttp3.Request
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.io.path.readBytes

interface MappingProvider {
    val LOGGER: Logger
    val url: String
    val shaUrl: String?
    val path: Path
    val version: String
    fun check(): Boolean {
        if (shaUrl == null) return true
        try {
            val sha = httpClient.newCall(Request.Builder().apply {
                url(shaUrl!!)
            }.build()).execute().body!!.string()

            val sha1 = MessageDigest.getInstance("SHA-1").digest(path.readBytes())
            val sha1Hex = sha1.joinToString("") { "%02x".format(it) }
            if (sha1Hex != sha) {
                LOGGER.error("Mappings for version $version(${this.javaClass.name}) are corrupt")
                path.toFile().delete()
                return false
            }
            return true
        } catch (e: IOException) {
            LOGGER.warn("Failed to check mappings for version $version(${this.javaClass.name}), but ignoring as it night be a network issue", e)
            return true
        }
    }
    fun download() {
        try {
            if (path.toFile().exists()) {
                if (check()) {
                    LOGGER.info("Mappings already exist for version $version")
                    return
                }
            }
            LOGGER.info("Downloading mappings for version $version")
            path.parent.createDirectories()
            httpClient.newCall(Request.Builder().apply {
                url(url)
            }.build()).execute().body!!.byteStream().use { input ->
                path.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (!check()) {
                LOGGER.error("Failed to download intermediary mappings for version $version: Checksum mismatch")
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to download intermediary mappings for version $version", e)
        }
    }
    fun load(): TinyTree
}
