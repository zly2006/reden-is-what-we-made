package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper
import com.github.zly2006.reden.utils.debugLogger
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import com.github.zly2006.reden.utils.setBlockNoPP
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.SpawnReason
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.tick.ChunkTickScheduler

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
        private fun operate(world: ServerWorld, record: PlayerData.UndoRedoRecord, redoRecord: PlayerData.RedoRecord?) {
            record.data.forEach { (posLong, entry) ->
                val pos = BlockPos.fromLong(posLong)
                val state = NbtHelper.toBlockState(Registries.BLOCK.readOnlyWrapper, entry.blockState)
                debugLogger("undo ${BlockPos.fromLong(posLong)}, $state")
                // set block
                world.setBlockNoPP(pos, state, Block.NOTIFY_LISTENERS)
                // clear schedules
                world.syncedBlockEventQueue.removeIf { it.pos == pos }
                val blockTickScheduler = world.getChunk(pos).blockTickScheduler as ChunkTickScheduler
                val fluidTickScheduler = world.getChunk(pos).fluidTickScheduler as ChunkTickScheduler
                blockTickScheduler.removeTicksIf { it.pos == pos }
                fluidTickScheduler.removeTicksIf { it.pos == pos }
                // apply block entity
                entry.blockEntity?.let { be ->
                    var blockEntity = world.getBlockEntity(BlockPos.fromLong(posLong))
                    if (blockEntity == null) {
                        try {
                            // force add block entities, got blocks like piston.
                            blockEntity = entry.blockEntityClazz!!
                                .getConstructor(BlockPos::class.java, BlockState::class.java)
                                .newInstance(pos, state).also(world::addBlockEntity)
                        } catch (e: Exception) {
                            Reden.LOGGER.error("Failed to create block entity for $pos, $state", e)
                        }
                    }
                    blockEntity?.readNbt(be)
                }
            }
            record.entities.forEach {
                if (it.value != null) {
                    val entry = it.value!!
                    val entity = world.getEntity(it.key)
                    if (entity != null) {
                        entity.readNbt(entry.nbt)
                    } else {
                        entry.entity.spawn(world, entry.nbt, null, entry.pos, SpawnReason.COMMAND, false, false)
                        redoRecord?.entities?.put(it.key, null) // add entity info to redo record
                    }
                }
                else {
                    world.getEntity(it.key)?.discard()
                }
            }
        }
        private fun <T: PlayerData.UndoRedoRecord> MutableList<T>.lastValid(): T? {
            while (this.isNotEmpty()) {
                val last = this.last()
                if (last.data.isNotEmpty() || last.entities.isNotEmpty()) {
                    return last
                }
                // if the last record is empty, remove it
                UpdateMonitorHelper.removeRecord(last.id)
                this.removeLast()
            }
            return null
        }
        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, res ->
                val view = player.data()
                fun sendStatus(status: Int) = res.sendPacket(Rollback(status))
                if (!view.canRecord) {
                    sendStatus(16)
                    return@registerGlobalReceiver
                }
                UpdateMonitorHelper.playerStopRecording(player)
                if (UpdateMonitorHelper.recording != null) {
                    Reden.LOGGER.error("Undo when a record is still active, id=" + UpdateMonitorHelper.recording?.id)
                    // 不取消跟踪会导致undo的更改也被记录，边读边写异常
                    UpdateMonitorHelper.undoRecords.clear()
                }
                when (packet.status) {
                    0 -> view.undo.lastValid()?.let { undoRecord ->
                        view.undo.removeLast()
                        UpdateMonitorHelper.removeRecord(undoRecord.id) // no longer monitoring rollbacked record
                        server.execute {
                            view.redo.add(
                                PlayerData.RedoRecord(
                                    id = undoRecord.id,
                                    lastChangedTick = -1,
                                    undoRecord = undoRecord
                                ).apply {
                                    data.putAll(undoRecord.data.keys.associateWith { posLong ->
                                        this.fromWorld( // add entity info to this redo record
                                            player.world,
                                            BlockPos.fromLong(posLong)
                                        )
                                    })
                                }
                            )
                            operate(player.serverWorld, undoRecord, view.redo.last())
                            sendStatus(0)
                        }
                    } ?: sendStatus(2)

                    1 -> view.redo.lastValid()?.let {
                        view.redo.removeLast()
                        server.execute {
                            operate(player.serverWorld, it, null)
                            view.undo.add(it.undoRecord)
                            sendStatus(1)
                        }
                    } ?: sendStatus(2)

                    else -> sendStatus(65536)
                }
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
