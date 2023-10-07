package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.TransferCooldownAccess
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.client.MinecraftClient
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
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                    val screen = MinecraftClient.getInstance().currentScreen
                    if (screen is TransferCooldownAccess) {
                        screen.transferCooldown = packet.cd
                    }
                    currentDelay = packet.cd
                    currentPos = packet.pos
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