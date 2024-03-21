package com.github.zly2006.reden.rvc

import kotlinx.serialization.Serializable
import net.minecraft.util.math.BlockPos

@Serializable
data class RelativeCoordinate(
    override val x: Int,
    override val y: Int,
    override val z: Int
): Coordinate {
    override fun getForOrigin(origin: Coordinate): Coordinate {
        return AbsoluteCoordinate(x + origin.x, y + origin.y, z + origin.z)
    }

    companion object {
        fun origin(origin: BlockPos) = RelativeBuilder(origin)

        class RelativeBuilder(private val origin: BlockPos) {
            fun block(pos: BlockPos) = RelativeCoordinate(pos.x - origin.x, pos.y - origin.y, pos.z - origin.z)
        }
    }
}
