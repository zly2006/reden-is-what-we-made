package com.github.zly2006.reden.transformers.mapping

import net.fabricmc.mapping.tree.TinyMappingFactory
import net.fabricmc.mapping.tree.TinyTree
import net.minecraft.MinecraftVersion
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.div

class Intermediary(
    gameDir: Path,
    override val version: String = MinecraftVersion.create().name
): MappingProvider {
    override val LOGGER = LoggerFactory.getLogger("Reden Intermediary Mapping")!!
    override val path = gameDir / ".cache" / "reden" / "mappings" / "intermediary-$version-v2.jar"
    override val url = "https://maven.fabricmc.net/net/fabricmc/intermediary/$version/intermediary-$version-v2.jar"
    override val shaUrl = "$url.sha1"
    override fun load(): TinyTree {
        FileSystems.newFileSystem(path).use {
            val mappings = it.getPath("/mappings/mappings.tiny")
            return Files.newBufferedReader(mappings).use { reader ->
                TinyMappingFactory.loadWithDetection(reader, true)
            }
        }
    }
}
