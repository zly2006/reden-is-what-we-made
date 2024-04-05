package com.github.zly2006.reden.rvc

import kotlinx.serialization.Serializable
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i

@Serializable
data class RelativeCoordinate(
    override val x: Int,
    override val y: Int,
    override val z: Int
): Coordinate {
    override fun getForOrigin(origin: Coordinate): Coordinate {
        return AbsoluteCoordinate(x + origin.x, y + origin.y, z + origin.z)
    }

    fun translate(rotation: BlockRotation): RelativeCoordinate {
        val (x, z) = when (rotation) {
            BlockRotation.CLOCKWISE_90 -> -y to x
            BlockRotation.CLOCKWISE_180 -> -x to -y
            BlockRotation.COUNTERCLOCKWISE_90 -> y to -x
            else -> x to z
        }
        return RelativeCoordinate(x, y, z)
    }

    fun translate(vec3i: Vec3i) = RelativeCoordinate(x + vec3i.x, y + vec3i.y, z + vec3i.z)

    fun translate(coordinate: Coordinate) = RelativeCoordinate(x + coordinate.x, y + coordinate.y, z + coordinate.z)

    fun invert() = RelativeCoordinate(-x, -y, -z)

    companion object {
        fun origin(origin: BlockPos) = RelativeBuilder(origin)

        class RelativeBuilder(private val origin: BlockPos) {
            fun block(pos: BlockPos) = RelativeCoordinate(pos.x - origin.x, pos.y - origin.y, pos.z - origin.z)
        }
    }
}
