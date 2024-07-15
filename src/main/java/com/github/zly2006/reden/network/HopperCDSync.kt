package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.utils.codec.BlockPosSerializer
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.network.packet.CustomPayload
import net.minecraft.screen.HopperScreenHandler
import net.minecraft.util.math.BlockPos

@Serializable
class HopperCDSync(
    @Serializable(BlockPosSerializer::class)
    val pos: BlockPos,
    val cd: Int
) : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<HopperCDSync> by PacketCodec(Reden.identifier("hopper_cd_sync")) {
        var currentPos: BlockPos? = null
        var currentDelay: Int = -1

        // query by position
        @JvmStatic
        fun clientQueryPacket(pos: BlockPos) = HopperCDSync(pos, 0)
        // query by screen
        @JvmStatic
        fun clientQueryPacket() = HopperCDSync(BlockPos.ORIGIN, 1)

        fun register() {
            PayloadTypeRegistry.playC2S().register(ID, CODEC)
            PayloadTypeRegistry.playS2C().register(ID, CODEC)
            ServerPlayNetworking.registerGlobalReceiver(ID) { packet, context ->
                when (packet.cd) {
                    0 -> {
                        val blockEntity = context.player().world.getBlockEntity(packet.pos)
                        if (blockEntity is HopperBlockEntity) {
                            context.responseSender().sendPacket(HopperCDSync(packet.pos, blockEntity.transferCooldown))
                        }
                    }
                    1 -> {
                        val sh = context.player().currentScreenHandler as? HopperScreenHandler
                        sh?.inventory?.let {
                            val blockEntity = it as HopperBlockEntity
                            context.responseSender().sendPacket(HopperCDSync(packet.pos, blockEntity.transferCooldown))
                        }
                    }
                }
            }
        }
    }
}
