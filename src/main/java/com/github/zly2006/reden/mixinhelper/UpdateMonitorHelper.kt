package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.BlockEntityInterface
import com.github.zly2006.reden.access.ChunkSectionInterface
import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.monitorSetBlock
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.playerStartRecording
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.playerStopRecording
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.popRecord
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.pushRecord
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.recordId
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.recording
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.undoRecords
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper.undoRecordsMap
import com.github.zly2006.reden.utils.debugLogger
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.state.BlockState

/**
 * # Undo
 *
 * This is the handler for undo feature.
 *
 * ## Players
 *
 * When players do some operation that is tracked by reden,
 * reden will create a backup (we call it `UndoRecord`, see [com.github.zly2006.reden.access.PlayerData.UndoRecord]) for it.
 * Player tracking starts at [playerStartRecording], and ends at [playerStopRecording].
 *
 * Then all changes will be recorded in the UndoRecord.
 * If the player wants to undo the operation, reden will restore the UndoRecord to the world.
 *
 * The id of the UndoRecord is unique (see [recordId]), and it will be used to identify the UndoRecord by [undoRecordsMap].
 *
 * ## Undo Record
 *
 * the current available UndoRecords are stored in [undoRecords].
 * It is a stack and the top is the current UndoRecord([recording]).
 *
 * ## Blocks
 *
 * Each time your world changes, reden will add the state before the change to the UndoRecord,
 * see [com.github.zly2006.reden.access.PlayerData.UndoRedoRecord.fromWorld].
 *
 * If you want to know how reden monitors block changes, see [monitorSetBlock].
 *
 * ## Entities
 *
 * For entities, we monitor all [net.minecraft.entity.data.TrackedData] changes.
 * see [com.github.zly2006.reden.mixin.undo.MixinDataTracker.beforeDataSet]
 *
 * ## Async changes
 *
 * (idk how to describe things like BE and scheduled tick that dont make changes immediately, just call them async changes)
 *
 * Primed TNTs, block events, scheduled ticks, they dont make changes immediately.
 *
 * So reden added a field [com.github.zly2006.reden.access.UndoableAccess.undoId] for them.
 *
 * When they are created, reden will assign them an undo id from [recording].
 *
 * When the async changes are applied, reden will check if the id is in the [undoRecordsMap].
 *
 * If it is, reden will push the specified UndoRecord to the stack by [pushRecord].
 * Then the game continues to process, making more changes.
 * And all changes will be recorded to the UndoRecord.
 * After the async changes are applied, reden will pop the UndoRecord from the stack by [popRecord].
 */
object UpdateMonitorHelper {
    class UndoRecordEntry(val id: Long, val record: PlayerData.UndoRecord?, val reason: String)
    private var recordId = 20060210L
    val undoRecordsMap: MutableMap<Long, PlayerData.UndoRecord> = HashMap()
    internal val undoRecords = mutableListOf<UndoRecordEntry>()

    /**
     * Used for crash recovery.
     */
    fun cleanup() {
        undoRecordsMap.clear()
        undoRecords.clear()
    }

    private fun filterLogById(undoId: Long) =
        undoId != 0L

    @JvmStatic
    fun pushRecord(id: Long, reasonSupplier: () -> String): Boolean {
        val reason = reasonSupplier()
        if (filterLogById(id))
            debugLogger("[${undoRecords.size + 1}] id $id: push, $reason")
        return undoRecords.add(
            UndoRecordEntry(
                id,
                undoRecordsMap[id],
                reason
            )
        )
    }
    @JvmStatic
    fun popRecord(reasonSupplier: () -> String): UndoRecordEntry {
        val reason = reasonSupplier()
        if (filterLogById(undoRecords.last().id))
            debugLogger("[${undoRecords.size}] id ${undoRecords.last().id}: pop, $reason")
        if (reason != undoRecords.last().reason) {
            throw IllegalStateException("Cannot pop record with different reason: $reason != ${undoRecords.last().reason}")
        }
        return undoRecords.removeLast()
    }
    val recording: PlayerData.UndoRecord? get() = undoRecords.lastOrNull()?.record

    /**
     * Monitor block changes.
     *
     * @param world the world where the block is changed
     * @param pos the position of the block
     * @param blockState only be `null` if the state does not change
     */
    @JvmStatic
    fun monitorSetBlock(world: ServerLevel, pos: BlockPos, blockState: BlockState) {
        debugLogger("id ${recording?.id ?: 0}: set$pos, ${world.getBlockState(pos)} -> $blockState")
        // update modified time, so undo can work properly
        world.modified(pos)

        recording?.data?.computeIfAbsent(pos.asLong()) {
            recording!!.fromWorld(world, pos, true)
        }
        recording?.lastChangedTick = world.server.tickCount
    }

    fun ServerLevel.modified(pos: BlockPos, time: Int = server.tickCount) = getChunk(pos).run {
        //? if <= 1.21.1
        /*isUnsaved = true*/
        //? if >= 1.21.2
        markUnsaved()
        getSection(getSectionIndex(pos.y)) as ChunkSectionInterface
    }.setModifyTime(pos, time)

    /**
     * @param beChangeOnly if only block entities changed, we have not recorded this change in [monitorSetBlock],
     *   so we should record it here
     */
    @JvmStatic
    fun postSetBlock(world: ServerLevel, pos: BlockPos, finalState: BlockState, beChangeOnly: Boolean) {
        val be = world.getBlockEntity(pos) as BlockEntityInterface?
//        if (be != null && RedenCarpetSettings.Options.undoBlockEntities) {
        if (be != null) {
            val data = be.lastSavedNbt
            debugLogger("id ${recording?.id ?: 0}: set$pos, block entity lastSaved=$data")

            if (beChangeOnly) {
                world.modified(pos)
                recording?.data?.computeIfAbsent(pos.asLong()) {
                    debugLogger("id ${recording?.id ?: 0}: set$pos, block entity, applying lastSavedNbt")
                    recording!!.fromWorld(world, pos, true).let {
                        if (data != null) it.copy(blockEntity = data)
                        else it
                    }
                }
            }

            be.saveLastNbt()
            debugLogger("postSetBlock: done.")
        }
    }

    /**
     * 此函数有危险副作用
     *
     * 使用此函数将**立刻**产生缓存的副作用
     *
     * 此缓存可能在没有确认的情况下不经检查直接调用
     */
    private fun addRecord(
        cause: PlayerData.UndoRecord.Cause,
        player: ServerPlayer
    ): PlayerData.UndoRecord {
        if (undoRecords.size != 0) {
            throw IllegalStateException("Cannot add record when there is already one.")
        }
        val undoRecord = PlayerData.UndoRecord(
            id = recordId,
            lastChangedTick = player.server.tickCount,
            cause = cause
        )
        undoRecordsMap[recordId] = undoRecord
        recordId++
        return undoRecord
    }

    internal fun removeRecord(id: Long) = undoRecordsMap.remove(id)

    @Suppress("unused")
    @JvmStatic
    fun playerStartRecording(player: ServerPlayer) = playerStartRecording(player, PlayerData.UndoRecord.Cause.UNKNOWN)
    @JvmStatic
    fun playerStartRecording(
        player: ServerPlayer,
        cause: PlayerData.UndoRecord.Cause
    ) {
        val playerView = player.data()
        if (!playerView.canRecord) return
        if (!playerView.isRecording) {
            playerView.isRecording = true
            val record = addRecord(cause, player)
            playerView.undo.add(record)
            pushRecord(record.id) { "player recording/${player.scoreboardName}/$cause" }
        }
    }

    @JvmStatic
    fun playerStopRecording(player: ServerPlayer) {
        val playerView = player.data()
        if (playerView.isRecording) {
            playerView.isRecording = false
            popRecord { "player recording/${player.scoreboardName}/${recording?.cause}" }
            playerView.redo
                .onEach { removeRecord(it.id) }
                .clear()
            var sum = playerView.undo.map(PlayerData.UndoRecord::getMemorySize).sum()
            debugLogger("Undo size: $sum")
            val allowedUndoSizeInBytes = 30 * 1024 * 1024
            if (allowedUndoSizeInBytes >= 0) {
                while (sum > allowedUndoSizeInBytes) {
                    removeRecord(playerView.undo.first().id)
                    playerView.undo.removeFirst()
                    debugLogger("Undo size: $sum, removing.")
                    sum = playerView.undo.map(PlayerData.UndoRecord::getMemorySize).sum()
                }
            }
        }
    }

    private fun playerQuit(player: ServerPlayer) =
        player.data().undo.forEach { removeRecord(it.id) }

    @JvmStatic
    fun tryAddRelatedEntity(entity: Entity) {
        if (entity.noPhysics) return
        if (entity is ServerPlayer) return
        if (!isInitializingEntity) {
            if (filterLogById(recording?.id ?: 0))
                debugLogger("id ${recording?.id ?: 0}: add ${entity.uuid}, type ${entity.type.toShortString()}")
            recording?.entities?.computeIfAbsent(entity.uuid) {
                PlayerData.EntityEntryImpl(
                    entity.type,
                    CompoundTag().apply(entity::save),
                    entity.blockPosition()
                )
            }
        }
    }

    /**
     * starts at: [com.github.zly2006.reden.mixin.undo.MixinEntity.beforeEntitySpawn]
     *
     * ends at:   [com.github.zly2006.reden.mixin.undo.MixinServerWorld.afterSpawn]
     */
    @JvmField var isInitializingEntity = false

    @JvmStatic
    fun entitySpawned(entity: Entity) {
        if (entity is ServerPlayer) return
        if (filterLogById(recording?.id ?: 0))
            debugLogger("id ${recording?.id ?: 0}: spawn ${entity.uuid}, type ${entity.type.toShortString()}")
        recording?.entities?.putIfAbsent(entity.uuid, PlayerData.NotExistEntityEntry)
    }

    init {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> playerQuit(handler.player) }
    }
}
