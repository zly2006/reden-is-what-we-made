package com.github.zly2006.reden.wormhole

import com.github.zly2006.reden.debugger.breakpoint.BlockPosSerializer
import com.github.zly2006.reden.utils.codec.Vec3dSerializer
import kotlinx.serialization.Serializable
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
