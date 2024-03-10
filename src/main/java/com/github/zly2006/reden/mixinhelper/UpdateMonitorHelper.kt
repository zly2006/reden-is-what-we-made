package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.BlockEntityInterface
import com.github.zly2006.reden.access.ChunkSectionInterface
import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.gui.message.ClientMessageQueue
import com.github.zly2006.reden.malilib.DEBUG_LOGGER_IGNORE_UNDO_ID_0
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
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

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

    private fun filterLogById(undoId: Long) =
        undoId != 0L || if (isClient) !DEBUG_LOGGER_IGNORE_UNDO_ID_0.booleanValue else true

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
    data class Changed(
        val record: PlayerData.UndoRecord,
        val pos: BlockPos
    )
    var lastTickChanged: MutableSet<Changed> = hashSetOf(); private set
    var thisTickChanged: MutableSet<Changed> = hashSetOf(); private set
    val recording: PlayerData.UndoRecord? get() = undoRecords.lastOrNull()?.record

    /**
     * Monitor block changes.
     *
     * @param world the world where the block is changed
     * @param pos the position of the block
     * @param blockState only be `null` if the state does not change
     */
    @JvmStatic
    fun monitorSetBlock(world: ServerWorld, pos: BlockPos, blockState: BlockState) {
        debugLogger("id ${recording?.id ?: 0}: set$pos, ${world.getBlockState(pos)} -> $blockState")
        // update modified time, so undo can work properly
        world.modified(pos)

        recording?.data?.computeIfAbsent(pos.asLong()) {
            recording!!.fromWorld(world, pos, true)
        }
        if (isClient && recording != null && !recording!!.notified) {
            // Send a notification that maybe he wants undo.
            if (recording!!.data.size > 2) {
                recording!!.notified = true
                val key = "reden:undo"
                val buttonList = mutableListOf<ClientMessageQueue.Button>()
                val id = ClientMessageQueue.onceNotification(
                    key,
                    Reden.LOGO,
                    Text.literal("Reden Undo"),
                    Text.literal("Did you make it by mistake? Press Ctrl+Z to undo it!").formatted(Formatting.GOLD),
                    buttonList
                )
                buttonList.add(
                    ClientMessageQueue.Button(Text.literal("Got it")) {
                        ClientMessageQueue.dontShowAgain(key)
                        ClientMessageQueue.remove(id)
                    }
                )
            }
        }
        recording?.lastChangedTick = server.ticks
    }

    fun ServerWorld.modified(pos: BlockPos, time: Int = server.ticks) = getChunk(pos).run {
        setNeedsSaving(true)
        getSection(getSectionIndex(pos.y)) as ChunkSectionInterface
    }.setModifyTime(pos, time)

    /**
     * @param beChangeOnly if only block entities changed, we have not recorded this change in [monitorSetBlock],
     *   so we should record it here
     */
    @JvmStatic
    fun postSetBlock(world: ServerWorld, pos: BlockPos, finalState: BlockState, beChangeOnly: Boolean) {
        val be = world.getBlockEntity(pos) as BlockEntityInterface?
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
        cause: PlayerData.UndoRecord.Cause
    ): PlayerData.UndoRecord {
        if (undoRecords.size != 0) {
            throw IllegalStateException("Cannot add record when there is already one.")
        }
        val undoRecord = PlayerData.UndoRecord(
            id = recordId,
            lastChangedTick = server.ticks,
            cause = cause
        )
        undoRecordsMap[recordId] = undoRecord
        recordId++
        return undoRecord
    }

    internal fun removeRecord(id: Long) = undoRecordsMap.remove(id)

    @Suppress("unused")
    @JvmStatic
    fun playerStartRecording(player: ServerPlayerEntity) = playerStartRecording(player, PlayerData.UndoRecord.Cause.UNKNOWN)
    @JvmStatic
    fun playerStartRecording(
        player: ServerPlayerEntity,
        cause: PlayerData.UndoRecord.Cause
    ) {
        if (server.data.frozen) return
        val playerView = player.data()
        if (!playerView.canRecord) return
        if (!playerView.isRecording) {
            playerView.isRecording = true
            val record = addRecord(cause)
            playerView.undo.add(record)
            pushRecord(record.id) { "player recording/${player.nameForScoreboard}/$cause" }
        }
    }

    @JvmStatic
    fun playerStopRecording(player: ServerPlayerEntity) {
        val playerView = player.data()
        if (playerView.isRecording) {
            playerView.isRecording = false
            popRecord { "player recording/${player.nameForScoreboard}/${recording?.cause}" }
            playerView.redo
                .onEach { removeRecord(it.id) }
                .clear()
            var sum = playerView.undo.map(PlayerData.UndoRecord::getMemorySize).sum()
            debugLogger("Undo size: $sum")
            if (RedenCarpetSettings.Options.allowedUndoSizeInBytes >= 0) {
                while (sum > RedenCarpetSettings.Options.allowedUndoSizeInBytes) {
                    removeRecord(playerView.undo.first().id)
                    playerView.undo.removeFirst()
                    debugLogger("Undo size: $sum, removing.")
                    sum = playerView.undo.map(PlayerData.UndoRecord::getMemorySize).sum()
                }
            }
        }
    }

    private fun playerQuit(player: ServerPlayerEntity) =
        player.data().undo.forEach { removeRecord(it.id) }

    @JvmStatic
    fun tryAddRelatedEntity(entity: Entity) {
        if (entity.noClip) return
        if (entity is ServerPlayerEntity) return
        if (!isInitializingEntity) {
            if (filterLogById(recording?.id ?: 0))
                debugLogger("id ${recording?.id ?: 0}: add ${entity.uuid}, type ${entity.type.name}")
            recording?.entities?.computeIfAbsent(entity.uuid) {
                PlayerData.EntityEntryImpl(
                    entity.type,
                    NbtCompound().apply(entity::writeNbt),
                    entity.blockPos
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
        if (entity is ServerPlayerEntity) return
        if (filterLogById(recording?.id ?: 0))
            debugLogger("id ${recording?.id ?: 0}: spawn ${entity.uuid}, type ${entity.type.name}")
        recording?.entities?.putIfAbsent(entity.uuid, PlayerData.NotExistEntityEntry)
    }

    init {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> playerQuit(handler.player) }
        ServerTickEvents.START_SERVER_TICK.register {
            lastTickChanged = thisTickChanged
            thisTickChanged = hashSetOf()
        }
    }
}
