package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.HopperScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

class HopperCDSync(
    val pos: BlockPos,
    val cd: Int
): FabricPacket {
    companion object {
        val id = Identifier(Reden.MOD_ID, "hopper_cd_sync")
        val pType = PacketType.create(id) {
            HopperCDSync(it.readBlockPos(), it.readVarInt())
        }!!
        var currentPos: BlockPos? = null
        var currentDelay: Int = -1

        // query by position
        @JvmStatic
        fun clientQueryPacket(pos: BlockPos) = HopperCDSync(pos, 0)
        // query by screen
        @JvmStatic
        fun clientQueryPacket() = HopperCDSync(BlockPos.ORIGIN, 1)

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                when (packet.cd) {
                    0 -> {
                        val blockEntity = player.world.getBlockEntity(packet.pos)
                        if (blockEntity is HopperBlockEntity) {
                            sender.sendPacket(HopperCDSync(packet.pos, blockEntity.transferCooldown))
                        }
                    }
                    1 -> {
                        val sh = player.currentScreenHandler as? HopperScreenHandler
                        sh?.inventory?.let {
                            val blockEntity = it as HopperBlockEntity
                            sender.sendPacket(HopperCDSync(packet.pos, blockEntity.transferCooldown))
                        }
                    }
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
        buf.writeVarInt(cd)
    }

    override fun getType() = pType
}