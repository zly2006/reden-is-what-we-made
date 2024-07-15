package com.github.zly2006.reden.utils.codec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.math.BlockPos

object BlockPosSerializer : KSerializer<BlockPos> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor = listSerialDescriptor<Int>()
    override fun deserialize(decoder: Decoder): BlockPos {
        val list = decoder.decodeSerializableValue(ListSerializer(Int.serializer()))
        return BlockPos(list[0], list[1], list[2])
    }

    override fun serialize(encoder: Encoder, value: BlockPos) {
        encoder.encodeSerializableValue(ListSerializer(Int.serializer()), listOf(value.x, value.y, value.z))
    }
}
