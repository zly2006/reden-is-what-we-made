package com.github.zly2006.reden.network

import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class TagBlockPos(
    val world: Identifier,
    val pos: BlockPos,
    val status: Int
): FabricPacket {
    override fun getType(): PacketType<*> = pType
    override fun write(buf: PacketByteBuf) {
        buf.writeIdentifier(world)
        buf.writeBlockPos(pos)
        buf.writeVarInt(status)
    }

    companion object {
        const val clear = 0
        const val green = 1
        const val red = 2

        val pType = run {
            PacketType.create(TAG_BLOCK_POS) {
                TagBlockPos(
                    it.readIdentifier(),
                    it.readBlockPos(),
                    it.readVarInt()
                )
            }
        }

        fun register() {
            if (!isClient) return
            ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> BlockBorder.tags.clear()}
            ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                BlockBorder.tags[packet.pos.asLong()] = packet.status
            }
        }
    }
}

private operator fun Vec3d.minus(pos: Vec3d): Vec3d {
    return Vec3d(x - pos.x, y - pos.y, z - pos.z)
}

private fun BlockPos.vec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
