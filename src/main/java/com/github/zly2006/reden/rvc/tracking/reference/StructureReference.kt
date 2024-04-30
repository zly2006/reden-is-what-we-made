package com.github.zly2006.reden.rvc.tracking.reference

import com.github.zly2006.reden.rvc.RelativeCoordinate
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos

@Serializable
class StructureReference(
    val name: String,
    @Contextual
    val referencedPart: TrackedStructurePart,
) {
    companion object {
        val nextId = (0L..Long.MAX_VALUE).iterator()
    }

    val id = nextId.next()
    val referrers: MutableList<Referrer> = mutableListOf()

    @Serializable
    data class Referrer(
        @Contextual
        val referencedPart: TrackedStructurePart? = null,
        val pos: RelativeCoordinate,
        val rotation: BlockRotation
    ) {
        val id = nextId.next()
    }

    enum class SyncMode {
        ChangedBlocks,
        AllBlocks,
        ChangedBlockEntities,
        AllBlockEntities,
        Entities,
    }

    fun sync(mode: SyncMode) {

    }

    data class Repeating(
        val part: TrackedStructurePart,
        val name: String,
        val startPos: BlockPos,
        val x: IntRange,
        val z: IntRange,
        val repeatX: Boolean,
        val repeatZ: Boolean,
        val offsetX: Int,
        val offsetZ: Int,
        val rotation: BlockRotation
    ) {
        fun build() = StructureReference(name, part).apply {
            val xRange = if (repeatX) x else 0..0
            val zRange = if (repeatZ) z else 0..0
            for (x in xRange) {
                for (z in zRange) {
                    referrers.add(
                        Referrer(
                            part,
                            RelativeCoordinate(
                                startPos.x + x * offsetX + x * part.xSize,
                                startPos.y,
                                startPos.z + z * offsetZ + z * part.zSize,
                            ), rotation
                        )
                    )
                }
            }
        }
    }

    enum class Type(val isSource: Boolean) {
        None(false),
        Source(true),
        Single(false),
        Repeating(false),
    }
}
