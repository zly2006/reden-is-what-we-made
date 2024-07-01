package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class TagBlockPos(
    val world: Identifier,
    val pos: BlockPos,
    val status: Int
) : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<TagBlockPos> by PacketCodec(Reden.identifier("tag_block_pos")) {
        const val clear = 0
        const val green = 1
        const val red = 2

        fun register() {
            if (!isClient) return
            PayloadTypeRegistry.playS2C().register(ID, CODEC)
            ClientPlayNetworking.registerGlobalReceiver(ID) { packet, _ ->
                BlockBorder[packet.pos] = packet.status
            }
        }
    }
}

private operator fun Vec3d.minus(pos: Vec3d): Vec3d {
    return Vec3d(x - pos.x, y - pos.y, z - pos.z)
}
