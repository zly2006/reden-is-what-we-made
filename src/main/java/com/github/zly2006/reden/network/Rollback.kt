package com.github.zly2006.reden.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.malilib.DEBUG_LOGGER
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.sendMessage
import com.github.zly2006.reden.utils.setBlockNoPP
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

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
        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, res ->
                val view = player.data()
                UpdateMonitorHelper.playerStopRecording(player)
                fun operate(record: PlayerData.UndoRecord): PlayerData.UndoRecord {
                    UpdateMonitorHelper.removeRecord(record.id) // no longer monitoring rollbacked record
                    val ret = PlayerData.UndoRecord(record.id, record.data.keys.associateWith {
                        PlayerData.Entry.fromWorld(
                            player.world,
                            BlockPos.fromLong(it)
                        )
                    }.toMutableMap())
                    record.data.forEach { (pos, entry) ->
                        val state = NbtHelper.toBlockState(Registries.BLOCK.readOnlyWrapper, entry.blockState)
                        if (DEBUG_LOGGER.booleanValue) {
                            player.sendMessage("undo ${BlockPos.fromLong(pos)}, $state")
                        }
                        player.world.setBlockNoPP(
                            BlockPos.fromLong(pos),
                            state,
                            Block.NOTIFY_LISTENERS
                        )
                        entry.blockEntity?.let { be ->
                            player.world.getBlockEntity(BlockPos.fromLong(pos))?.readNbt(be)
                        }
                    }
                    return ret
                }
                fun MutableList<PlayerData.UndoRecord>.lastValid(): PlayerData.UndoRecord? {
                    while (this.isNotEmpty()) {
                        val last = this.last()
                        if (last.data.isNotEmpty()) {
                            return last
                        }
                        UpdateMonitorHelper.removeRecord(last.id)
                        this.removeLast()
                    }
                    return null
                }
                res.sendPacket(Rollback(when (packet.status) {
                    0 -> view.undo.lastValid()?.let {
                        view.undo.removeLast()
                        view.redo.add(operate(it))
                        0
                    } ?: 2

                    1 -> view.redo.lastValid()?.let {
                        view.redo.removeLast()
                        view.undo.add(operate(it))
                        1
                    } ?: 2

                    else -> 65536
                }))
            }
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, res ->
                    player.sendMessage(
                        when (packet.status) {
                            0 -> Text.literal("[Reden/Undo] Rollback success")
                            1 -> Text.literal("[Reden/Undo] Restore success")
                            2 -> Text.literal("[Reden/Undo] No blocks info")
                            16 -> Text.literal("[Reden/Undo] No permission")
                            32 -> Text.literal("[Reden/Undo] Not recording")
                            65536 -> Text.literal("[Reden/Undo] Unknown error")
                            else -> Text.literal("[Reden/Undo] Unknown status")
                        }
                    )
                }
            }
        }
    }
}
