package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.access.UndoRecordContainer
import com.github.zly2006.reden.carpet.RedenCarpetSettings
import com.github.zly2006.reden.utils.debugLogger
import com.github.zly2006.reden.utils.server
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.block.ChainRestrictedNeighborUpdater

object UpdateMonitorHelper: UndoRecordContainer {
    private val listeners: MutableMap<World.(ChainRestrictedNeighborUpdater.Entry) -> Unit, LifeTime> = mutableMapOf()
    private val chainFinishListeners = mutableMapOf<World.() -> Unit, LifeTime>()
    private var recordId = 20060210L
    val undoRecordsMap: MutableMap<Long, PlayerData.UndoRecord> = HashMap()
    data class Changed(
        val record: PlayerData.UndoRecord,
        val pos: BlockPos
    )
    var lastTickChanged: MutableSet<Changed> = hashSetOf(); private set
    var thisTickChanged: MutableSet<Changed> = hashSetOf(); private set

    /**
     * 非常非常危险的变量，如果没有十足把握请不要直接操作
     *
     * 这会带来不经检查的访问
     */
    override var recording: PlayerData.UndoRecord? = null
        set(value) {
            if (field != value) {
                field = value
                if (value == null) {
                    debugLogger("record canceled")
                } else {
                    debugLogger("record start")
                }
            }
        }
    enum class LifeTime {
        PERMANENT,
        TICK,
        CHAIN,
        ONCE
    }

    @JvmStatic
    fun startMonitor(onUpdate: World.(ChainRestrictedNeighborUpdater.Entry) -> Unit, lifeTime: LifeTime) {
        listeners[onUpdate] = lifeTime
    }

    @JvmStatic
    fun onUpdate(world: World, entry: ChainRestrictedNeighborUpdater.Entry) {
        //debugLogger("UpdateMonitorHelper.onUpdate")
        listeners.forEach { (k, v) ->
            k.invoke(world, entry)
            if (v == LifeTime.ONCE) {
                listeners.remove(k)
            }
        }
    }

    @JvmStatic
    fun onChainFinish(world: World) {
        //debugLogger("UpdateMonitorHelper.finish")
        listeners.forEach { (k, v) ->
            if (v == LifeTime.CHAIN) {
                listeners.remove(k)
            }
        }
        chainFinishListeners.forEach { (k, v) ->
            k.invoke(world)
            if (v == LifeTime.ONCE || v == LifeTime.CHAIN) {
                chainFinishListeners.remove(k)
            }
        }
    }

    @JvmStatic
    fun monitorSetBlock(world: ServerWorld, pos: BlockPos, blockState: BlockState) {
        debugLogger("id ${recording?.id ?: 0}: set$pos, ${world.getBlockState(pos)} -> $blockState")
        recording?.data?.computeIfAbsent(pos.asLong()) {
            recording!!.fromWorld(world, pos)
        }
        recording?.lastChangedTick = server.ticks
    }

    /**
     * 此函数有危险副作用
     *
     * 使用此函数将**立刻**产生缓存的副作用
     *
     * 此缓存可能在没有确认的情况下不经检查直接调用
     */
    private fun addRecord(): PlayerData.UndoRecord {
        recording = PlayerData.UndoRecord(
            id = recordId,
            lastChangedTick = server.ticks,
        )
        undoRecordsMap[recordId] = recording!!
        recordId++
        return recording!!
    }

    internal fun removeRecord(id: Long) = undoRecordsMap.remove(id)
    @JvmStatic
    fun playerStartRecord(player: ServerPlayerEntity) {
        val playerView = player.data()
        if (!playerView.canRecord) return
        if (!playerView.isRecording) {
            playerView.isRecording = true
            playerView.undo.add(addRecord())
        }
    }

    fun playerStopRecording(player: ServerPlayerEntity) {
        val playerView = player.data()
        if (playerView.isRecording) {
            playerView.isRecording = false
            recording = null
            playerView.redo
                .onEach { removeRecord(it.id) }
                .clear()
            var sum = playerView.undo.map(PlayerData.UndoRecord::getMemorySize).sum()
            debugLogger("Undo size: $sum")
            if (RedenCarpetSettings.allowedUndoSizeInBytes >= 0) {
                while (sum > RedenCarpetSettings.allowedUndoSizeInBytes) {
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
        // not used
    }

    @JvmStatic
    fun entitySpawned(entity: Entity) {
        if (entity is ServerPlayerEntity) return
        recording?.entities?.put(entity.uuid, null)
    }

    init {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> playerQuit(handler.player) }
        ServerTickEvents.START_SERVER_TICK.register {
            lastTickChanged = thisTickChanged
            thisTickChanged = hashSetOf()
        }
    }
}