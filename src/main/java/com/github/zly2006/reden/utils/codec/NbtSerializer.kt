package com.github.zly2006.reden.utils.codec

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

object NbtSerializer : KSerializer<CompoundTag> {
    override val descriptor = PrimitiveSerialDescriptor("minecraft.NbtCompound", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): CompoundTag = NbtIo.read(
        DataInputStream(ByteArrayInputStream(decoder.decodeSerializableValue(ByteArraySerializer()))),
        NbtAccounter.create(1024 * 1024) // 1 MB
    ) as CompoundTag

    override fun serialize(encoder: Encoder, value: CompoundTag) {
        val stream = ByteArrayOutputStream()
        NbtIo.write(value, DataOutputStream(stream))
        encoder.encodeSerializableValue(ByteArraySerializer(), stream.toByteArray())
    }
}
