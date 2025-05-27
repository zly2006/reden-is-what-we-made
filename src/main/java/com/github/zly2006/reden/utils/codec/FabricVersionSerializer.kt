package com.github.zly2006.reden.utils.codec

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.fabricmc.loader.api.Version

object FabricVersionSerializer : KSerializer<Version> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("fabric.api.Version", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Version {
        return Version.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Version) {
        encoder.encodeString(value.toString())
    }
}
