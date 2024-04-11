package com.github.zly2006.reden.utils.codec

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = String.serializer().descriptor
    override fun deserialize(decoder: Decoder) = UUID.fromString(decoder.decodeString())!!
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}
