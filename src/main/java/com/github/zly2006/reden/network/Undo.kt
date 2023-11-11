package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper
import com.github.zly2006.reden.utils.*
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.mob.MobEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.tick.ChunkTickScheduler

class Undo(
    val status: Int = 0
): FabricPacket {
    override fun getType(): PacketType<*> = pType
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(status)
    }

    companion object {
        val id = Reden.identifier("undo")
        private val pType = PacketType.create(id) {
            Undo(it.readVarInt())
        }
        private fun operate(world: ServerWorld, record: PlayerData.UndoRedoRecord, redoRecord: PlayerData.RedoRecord?) {
            record.data.forEach { (posLong, entry) ->
                val pos = BlockPos.fromLong(posLong)
                debugLogger("undo ${BlockPos.fromLong(posLong)}, ${entry.state}")
                // set block
                world.setBlockNoPP(pos, entry.state, Block.NOTIFY_LISTENERS)
                // clear schedules
                world.syncedBlockEventQueue.removeIf { it.pos == pos }
                val blockTickScheduler = world.getChunk(pos).blockTickScheduler as ChunkTickScheduler
                val fluidTickScheduler = world.getChunk(pos).fluidTickScheduler as ChunkTickScheduler
                blockTickScheduler.removeTicksIf { it.pos == pos }
                fluidTickScheduler.removeTicksIf { it.pos == pos }
                // apply block entity
                entry.blockEntity?.let { beNbt ->
                    world.addBlockEntity(BlockEntity.createFromNbt(pos, entry.state, beNbt))
                }
            }
            record.entities.forEach {
                val entity = world.getEntity(it.key)
                if (entity == null) {
                    if (it.value != PlayerData.NotExistEntityEntry) {
                        val entry = it.value
                        debugLogger("undo entity ${it.key} spawning")
                        val newEntity = entry.entity!!.spawn(world, null, null, entry.pos, SpawnReason.COMMAND, false, false)
                        if (newEntity != null) {
                            newEntity.readNbt(entry.nbt)
                            // Note: uuid is different from the original one
                            //  (No undo record should be here, so dont worry about it)
                            newEntity.uuid = it.key
                            redoRecord?.entities?.put(it.key, PlayerData.NotExistEntityEntry) // add entity info to redo record
                        }
                    }
                } else {
                    redoRecord?.entities?.put(
                        it.key, PlayerData.EntityEntryImpl(
                            entity.type,
                            entity.writeNbt(NbtCompound()),
                            entity.blockPos
                        )
                    )
                    if (it.value == PlayerData.NotExistEntityEntry) {
                        debugLogger("undo entity ${it.key} removing")
                        entity.discard()
                    } else {
                        val entry = it.value
                        debugLogger("undo entity ${it.key} reading nbt")
                        entity.readNbt(entry.nbt)
                        if (entity is MobEntity) {
                            entity.clearGoalsAndTasks()
                        }
                    }
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
                fun sendStatus(status: Int) = res.sendPacket(Undo(status))
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
                                            BlockPos.fromLong(posLong),
                                            true
                                        )
                                    })
                                    entities.clear()
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
                            0 -> translateMessage("undo", "rollback_success")
                            1 -> translateMessage("undo", "restore_success")
                            2 -> translateMessage("undo", "no_blocks_info")
                            16 -> translateMessage("undo", "no_permission")
                            32 -> translateMessage("undo", "not_recording")
                            64 -> translateMessage("undo", "busy")
                            65536 -> translateMessage("undo", "unknown_error")
                            else -> translateMessage("undo", "unknown_status")
                        }
                    )
                }
            }
        }
    }
}
