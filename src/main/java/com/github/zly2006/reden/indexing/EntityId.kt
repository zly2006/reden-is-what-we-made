package com.github.zly2006.reden.indexing

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.io.File
import java.net.URI

@OptIn(ExperimentalSerializationApi::class)
class EntityId(definition: URI = URI("https://www.redenmc.com/api/data/entity_ids.json")) {
    val index = Json.decodeFromStream<Map<String, Int>>(definition.toURL().openStream())

    fun of(identifier: Identifier): Int {
        return index[identifier.toString()] ?: 0
    }

    fun of(block: EntityType<*>): Int {
        return of(Registries.ENTITY_TYPE.getId(block))
    }

    fun checkExtra(output: File?): List<Identifier> {
        val extra = Registries.ENTITY_TYPE.ids.filter { it.toString() !in index }
        output?.let {
            val copy = index.toMutableMap()
            extra.forEach { copy[it.toString()] = copy.size }
            output.writeText(Json.encodeToString(copy))
        }
        return extra
    }
}
