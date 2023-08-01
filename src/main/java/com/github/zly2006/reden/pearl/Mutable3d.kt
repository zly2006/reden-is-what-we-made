package com.github.zly2006.reden.pearl

import net.minecraft.util.math.Vec3d

class Mutable3d(x: Double, y: Double, z: Double) : Vec3d(x, y, z) {
    constructor() : this(0.0, 0.0, 0.0)
    constructor(vec3d: Vec3d) : this(vec3d.x, vec3d.y, vec3d.z)

    override fun add(x: Double, y: Double, z: Double): Vec3d {
        this.x += x
        this.y += y
        this.z += z
        return this
    }

    override fun multiply(x: Double, y: Double, z: Double): Vec3d {
        this.x *= x
        this.y *= y
        this.z *= z
        return this
    }
}