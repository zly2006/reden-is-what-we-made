package com.github.zly2006.reden.network

import com.github.zly2006.reden.access.PlayerPatchesView
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.text.Text

private val pType = PacketType.create(ROLLBACK) {
    Rollback(it.readVarInt())
}
class Rollback(
    val status: Int = 0
): FabricPacket {
    override fun getType(): PacketType<*> = pType
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(status)
    }
    companion object {
        init {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, res ->
                val view = player as PlayerPatchesView
                view.isRecording = false
                view.blocks.lastOrNull()?.let {
                    it.forEach { (pos, entry) ->
                        player.world.setBlockState(pos, NbtHelper.toBlockState(Registries.BLOCK.readOnlyWrapper, entry.blockState))
                        entry.blockEntity?.let { be ->
                            player.world.getBlockEntity(pos)?.readNbt(be)
                        }
                    }
                    res.sendPacket(Rollback(0)) // success
                    view.blocks.removeLast()
                } ?: res.sendPacket(Rollback(2)) // no blocks info
            }
            ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, res ->
                if (packet.status == 0) {
                    player.sendMessage(Text.literal("Rollback success"), false)
                }
                if (packet.status == 2) {
                    player.sendMessage(Text.literal("No blocks info"), false)
                }
            }
        }
    }
}
