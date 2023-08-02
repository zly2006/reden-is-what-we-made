package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.Person
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import net.minecraft.util.math.BlockPos

/**
 * Dont use
 */
object DiffSerializer: KSerializer<TrackedDiff> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("TrackedDiff") {
            element("parentIds", LongArraySerializer().descriptor)
            element("originDiff", ArraySerializer(BlockPosSerializer).descriptor)
            element("xSize", Int.serializer().descriptor)
            element("ySize", Int.serializer().descriptor)
            element("zSize", Int.serializer().descriptor)
            element("message", String.serializer().descriptor)
            element("timestamp", Long.serializer().descriptor)
        }

    override fun deserialize(decoder: Decoder): TrackedDiff {
        return decoder.decodeStructure(descriptor) {
            var parentIds: LongArray? = null
            var originDiff: Array<BlockPos>? = null
            var xSize: Int? = null
            var ySize: Int? = null
            var zSize: Int? = null
            var message: String? = null
            var timestamp: Long? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> parentIds = decodeSerializableElement(descriptor, index, LongArraySerializer())
                    1 -> originDiff = decodeSerializableElement(descriptor, index, ArraySerializer(BlockPosSerializer))
                    2 -> xSize = decodeIntElement(descriptor, index)
                    3 -> ySize = decodeIntElement(descriptor, index)
                    4 -> zSize = decodeIntElement(descriptor, index)
                    5 -> message = decodeStringElement(descriptor, index)
                    6 -> timestamp = decodeLongElement(descriptor, index)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            TrackedDiff(
                parentIds ?: error("Missing parentIds"),
                originDiff ?: error("Missing originDiff"),
                xSize ?: error("Missing xSize"),
                ySize ?: error("Missing ySize"),
                zSize ?: error("Missing zSize"),
                emptyMap(),
                emptyMap(),
                emptySet(),
                emptyMap(),
                message ?: error("Missing message"),
                timestamp ?: error("Missing timestamp"),
                Person("Unknown", "Unknown", null, "Unknown")
            )
        }
    }

    override fun serialize(encoder: Encoder, value: TrackedDiff) {
        TODO("Not yet implemented")
    }
}

object BlockPosSerializer: KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor
        get() = Long.serializer().descriptor

    override fun deserialize(decoder: Decoder): BlockPos {
        val l = decoder.decodeLong()
        return BlockPos.fromLong(l)
    }

    override fun serialize(encoder: Encoder, value: BlockPos) {
        encoder.encodeLong(value.asLong())
    }
}
