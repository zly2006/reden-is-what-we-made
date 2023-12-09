package com.github.zly2006.reden.transformers.mapping

import com.github.zly2006.reden.report.httpClient
import net.fabricmc.mapping.tree.TinyMappingFactory
import net.fabricmc.mapping.tree.TinyTree
import net.minecraft.MinecraftVersion
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.div


class Yarn(
    gameDir: Path,
    override val version: String = MinecraftVersion.create().name
): MappingProvider {
    override val LOGGER = LoggerFactory.getLogger("Reden Yarn Mapping")
    override val url = run {
        val versions = httpClient.newCall(Request.Builder().apply {
            url("https://maven.fabricmc.net/net/fabricmc/yarn/maven-metadata.xml")
        }.build()).execute().body.use {
            // read xml
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(it!!.byteStream())
            val versions = doc.getElementsByTagName("version")
            (0 until versions.length).map { versions.item(it).childNodes.item(0).textContent }
        }
        val version = versions.last { it.startsWith(version) }
        "https://maven.fabricmc.net/net/fabricmc/yarn/$version/yarn-$version-v2.jar"
    }
    override val shaUrl = "$url.sha1"
    override val path = gameDir / ".cache" / "reden" / "mappings" / "yarn-mapping-$version.jar"

    override fun load(): TinyTree {
        FileSystems.newFileSystem(path).use {
            val mappings = it.getPath("/mappings/mappings.tiny")
            return Files.newBufferedReader(mappings).use { reader ->
                TinyMappingFactory.loadWithDetection(reader, true)
            }
        }
    }
}
