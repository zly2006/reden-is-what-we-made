package com.github.zly2006.reden.utils.codec

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtSizeTracker
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

object NbtSerializer : KSerializer<NbtCompound> {
    override val descriptor = ByteArraySerializer().descriptor

    override fun deserialize(decoder: Decoder): NbtCompound = NbtIo.read(
        DataInputStream(ByteArrayInputStream(decoder.decodeSerializableValue(ByteArraySerializer()))),
        NbtSizeTracker.ofUnlimitedBytes()
    ) as NbtCompound

    override fun serialize(encoder: Encoder, value: NbtCompound) {
        val stream = ByteArrayOutputStream()
        NbtIo.write(value, DataOutputStream(stream))
        encoder.encodeSerializableValue(ByteArraySerializer(), stream.toByteArray())
    }
}
