package com.github.zly2006.reden.rvc.gui.reference

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.template.RvcTemplate
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.readText

class RvcUseTemplateScreen(path: Path) {
    val template = Json.decodeFromString<RvcTemplate>(path.resolve("rvc_template.json").readText())

    init {
    }

    companion object {
        fun fromClasspath(path: String) = RvcUseTemplateScreen(
            Path.of(
                Reden::class.java.classLoader.getResource(path)?.toURI()
                    ?: throw IllegalArgumentException("Resource not found: $path")
            )
        )
    }
}
