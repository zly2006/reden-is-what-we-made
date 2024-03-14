package com.github.zly2006.reden.indexing

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.utils.openStreamRetrying
import com.github.zly2006.reden.utils.redenApiBaseUrl
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.state.property.Properties
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.URI

private val Field.static: Boolean
    get() = modifiers and Modifier.STATIC != 0

typealias BlockProperty = net.minecraft.state.property.Property<*>

class BlockPropertyId(definition: URI = URI("$redenApiBaseUrl/data/property_ids.json")) : Index<BlockProperty> {
    @Serializable
    data class Property(
        val values: List<String>,
        val name: String = "dummy",
        val className: String,
        val id: Int
    ) {
        @Transient
        var instance: BlockProperty? = null
    }

    @OptIn(ExperimentalSerializationApi::class)
    val properties = Json.decodeFromStream<List<Property>>(definition.toURL().openStreamRetrying())
    init {
        checkExtra(null)
    }
    val index = properties.mapNotNull { it.instance?.to(it) }.toMap()
    override fun of(identifier: BlockProperty): Int {
        return index[identifier]?.id ?: 0
    }

    override fun checkExtra(output: File?): List<BlockProperty> {
        val clazz = net.minecraft.state.property.Property::class.java
        val ret = mutableListOf<BlockProperty>()
        val blockProperties = Properties::class.java.fields.asSequence()
            .filter { it.static && clazz.isAssignableFrom(it.type) }
            .map { it.get(null) as BlockProperty }
            .toMutableList()
        val defs = properties.toMutableList()
        while (blockProperties.isNotEmpty()) {
            val blockProperty = blockProperties.removeLast()
            val property = defs.firstOrNull { matches(blockProperty, it) }
            if (property == null) {
                ret.add(blockProperty)
                Reden.LOGGER.warn("failed to find block property definition for " + create(blockProperty))
            }
            else {
                defs.remove(property)
                property.instance = blockProperty
            }
        }
        defs.forEach {
            Reden.LOGGER.warn("failed to find block property instance for $it")
        }
        return ret
    }

    private fun <T : Comparable<T>> matches(property: net.minecraft.state.property.Property<T>, def: Property): Boolean {
        if (property.name != def.name) {
            return false
        }
        if (property.values.map { property.name(it) } != def.values) {
            return false
        }
        return true
    }

    fun <T : Comparable<T>> create(property: net.minecraft.state.property.Property<T>): Property {
        return Property(
            property.values.map { property.name(it) },
            property.name,
            property.javaClass.simpleName,
            -1
        )
    }
}
