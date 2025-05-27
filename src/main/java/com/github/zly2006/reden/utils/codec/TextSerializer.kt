package com.github.zly2006.reden.utils.codec

import com.github.zly2006.reden.utils.server
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object TextSerializer : KSerializer<MutableComponent> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("minecraft.Text", PrimitiveKind.STRING)

    //todo
    private val registryManager get() = server.registryAccess()

    override fun deserialize(decoder: Decoder): MutableComponent {
        return Component.Serializer.fromJson(decoder.decodeString(), registryManager)!!
    }

    override fun serialize(encoder: Encoder, value: MutableComponent) {
        encoder.encodeString(Component.Serializer.toJson(value, registryManager))
    }
}
