package com.github.zly2006.reden.utils.codec

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.world.phys.Vec3

object Vec3dSerializer : KSerializer<Vec3> {
    override val descriptor = DoubleArraySerializer().descriptor

    override fun deserialize(decoder: Decoder): Vec3 {
        val doubles = decoder.decodeSerializableValue(DoubleArraySerializer())
        require(doubles.size == 3)
        return Vec3(doubles[0], doubles[1], doubles[2])
    }

    override fun serialize(encoder: Encoder, value: Vec3) {
        encoder.encodeSerializableValue(DoubleArraySerializer(), doubleArrayOf(value.x, value.y, value.z))
    }
}
