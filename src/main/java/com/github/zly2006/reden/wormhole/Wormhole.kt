package com.github.zly2006.reden.wormhole

import com.github.zly2006.reden.debugger.breakpoint.BlockPosSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

@Serializable
data class Wormhole(
    @Serializable(with = BlockPosSerializer::class)
    val destination: BlockPos,
    val name: String,
    @Serializable(with = Vec3dSerializer::class)
    val tpPosition: Vec3d,
    val tpYaw: Float,
    val tpPitch: Float,
)

private object Vec3dSerializer : KSerializer<Vec3d> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = listSerialDescriptor<Double>()

    override fun deserialize(decoder: Decoder): Vec3d {
        val list = decoder.decodeSerializableValue(ListSerializer(Double.serializer()))
        require(list.size == 3) { "Vec3d must have 3 elements" }
        return Vec3d(list[0], list[1], list[2])
    }

    override fun serialize(encoder: Encoder, value: Vec3d) {
        encoder.encodeSerializableValue(ListSerializer(Double.serializer()), listOf(value.x, value.y, value.z))
    }
}
