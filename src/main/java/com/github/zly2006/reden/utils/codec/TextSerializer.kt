package com.github.zly2006.reden.utils.codec

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.MutableComponent

object TextSerializer : KSerializer<MutableComponent> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("minecraft.Text", PrimitiveKind.STRING)
    val GSON = Gson()

    fun strToText(str: String): MutableComponent {
        return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, GSON.fromJson(str, JsonElement::class.java)).orThrow.copy()
    }
    fun textToStr(text: Component): String {
        return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text).orThrow.toString()
    }

    override fun deserialize(decoder: Decoder): MutableComponent = strToText(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: MutableComponent) = encoder.encodeString(textToStr(value))
}
