package com.github.zly2006.reden.utils.codec

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.resources.ResourceLocation

object IdentifierSerializer : KSerializer<ResourceLocation> {
    override val descriptor = PrimitiveSerialDescriptor("reden.identifier", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = ResourceLocation.parse(decoder.decodeString())!!
    override fun serialize(encoder: Encoder, value: ResourceLocation) {
        encoder.encodeString(value.toString())
    }
}
