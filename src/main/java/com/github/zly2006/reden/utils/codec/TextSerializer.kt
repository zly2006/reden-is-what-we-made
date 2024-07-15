package com.github.zly2006.reden.utils.codec

import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.client.MinecraftClient
import net.minecraft.text.MutableText
import net.minecraft.text.Text

object TextSerializer : KSerializer<MutableText> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("minecraft.Text", PrimitiveKind.STRING)

    private val registryManager get() = if (isClient) MinecraftClient.getInstance().player?.registryManager else server.registryManager

    override fun deserialize(decoder: Decoder): MutableText {
        return Text.Serialization.fromJson(decoder.decodeString(), registryManager)!!
    }

    override fun serialize(encoder: Encoder, value: MutableText) {
        encoder.encodeString(Text.Serialization.toJsonString(value, registryManager))
    }
}
