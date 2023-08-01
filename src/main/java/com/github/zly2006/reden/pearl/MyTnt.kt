package com.github.zly2006.reden.pearl

import net.minecraft.util.math.Vec3d
import kotlin.math.sqrt


class MyTnt(
    val pos: Vec3d,
    val power: Int = 4
) {
    fun accelerate(pearl: MyEndPearlEntity) {
        val w = sqrt(pearl.pos.squaredDistanceTo(pos)) / (power * 2)
        if (w <= 1.0) {
            var x = pearl.pos.x - pos.x
            var y = pearl.eyeY - pos.y
            var z = pearl.pos.z - pos.z
            val aa = sqrt(x * x + y * y + z * z)
            if (aa != 0.0) {
                x /= aa
                y /= aa
                z /= aa
                val ad = (1.0 - w)
                x *= ad
                y *= ad
                z *= ad
                val vec3d2 = Vec3d(x, y, z)
                pearl.motion.add(vec3d2)
            }
        }
    }
}