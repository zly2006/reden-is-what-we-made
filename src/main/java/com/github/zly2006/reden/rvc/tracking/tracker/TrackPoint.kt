package com.github.zly2006.reden.rvc.tracking.tracker

import com.github.zly2006.reden.rvc.RelativeCoordinate
import com.github.zly2006.reden.rvc.blockPos
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import net.minecraft.util.math.BlockPos

@Serializable(TrackPoint.Companion.TrackPointSerializer::class)
class TrackPoint(
    val relativeCoordinate: RelativeCoordinate,
    predicate: TrackPredicate,
    mode: TrackPredicate.TrackMode
) : SpreadEntry(predicate, mode) {
    constructor(
        pos: BlockPos,
        part: TrackedStructurePart,
        predicate: TrackPredicate,
        mode: TrackPredicate.TrackMode
    ) : this(part.getRelativeCoordinate(pos), predicate, mode) {
        this.pos = pos
    }
    companion object {
        object TrackPointSerializer : KSerializer<TrackPoint> {
            override val descriptor = buildClassSerialDescriptor("TrackPoint") {
                element<String>("relativeCoordinate")
                element<TrackPredicate>("predicate")
                element<TrackPredicate.TrackMode>("mode")
            }

            override fun deserialize(decoder: Decoder): TrackPoint =
                decoder.decodeStructure(descriptor) {
                    var relativeCoordinate: RelativeCoordinate? = null
                    var predicate: TrackPredicate? = null
                    var mode: TrackPredicate.TrackMode? = null
                    while (true) {
                        when (val index = decodeElementIndex(descriptor)) {
                            0 -> relativeCoordinate = decoder.decodeSerializableValue(RelativeCoordinate.serializer())
                            1 -> predicate = decoder.decodeSerializableValue(TrackPredicate.serializer())
                            2 -> mode =
                                decodeSerializableElement(descriptor, index, TrackPredicate.TrackMode.serializer())

                            CompositeDecoder.DECODE_DONE -> break
                            else -> error("Unexpected index: $index")
                        }
                    }
                    return@decodeStructure TrackPoint(relativeCoordinate!!, predicate!!, mode!!)
                }

            override fun serialize(encoder: Encoder, value: TrackPoint) {
                encoder.encodeStructure(descriptor) {
                    encodeSerializableElement(descriptor, 0, RelativeCoordinate.serializer(), value.relativeCoordinate)
                    encodeSerializableElement(descriptor, 1, TrackPredicate.serializer(), value.predicate)
                    encodeSerializableElement(descriptor, 2, TrackPredicate.TrackMode.serializer(), value.mode)
                }
            }
        }
    }
    val maxElements: Int
        get() = when (mode) {
            TrackPredicate.TrackMode.IGNORE -> 5000
            TrackPredicate.TrackMode.TRACK -> 8000
            TrackPredicate.TrackMode.NOOP -> 0
        }

    fun updateOrigin(structure: TrackedStructurePart) {
        this.part = structure
        pos = relativeCoordinate.blockPos(structure.origin)
    }

    override fun toString(): String {
        return "TrackPoint(${pos.toShortString()}, $predicate, $mode)"
    }
}
