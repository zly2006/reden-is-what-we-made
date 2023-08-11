package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.PlayerData.Companion.data
import com.github.zly2006.reden.malilib.DEBUG_LOGGER
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.block.ChainRestrictedNeighborUpdater

object UpdateMonitorHelper {
    private val listeners: MutableMap<World.(ChainRestrictedNeighborUpdater.Entry) -> Unit, LifeTime> = mutableMapOf()
    private val chainFinishListeners = mutableMapOf<World.() -> Unit, LifeTime>()
    private var recordId = 20060210L
    val undoRecordsMap: MutableMap<Long, PlayerData.UndoRecord> = HashMap()
    var recording: PlayerData.UndoRecord? = null
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
        if (DEBUG_LOGGER.booleanValue && listeners.isNotEmpty()) {
            MinecraftClient.getInstance().player?.sendMessage(Text.literal("UpdateMonitorHelper.onUpdate"))
        }
        listeners.forEach { (k, v) ->
            k.invoke(world, entry)
            if (v == LifeTime.ONCE) {
                listeners.remove(k)
            }
        }
    }

    @JvmStatic
    fun onChainFinish(world: World) {
        if (DEBUG_LOGGER.booleanValue && (listeners + chainFinishListeners).isNotEmpty()) {
            MinecraftClient.getInstance().player?.sendMessage(Text.literal("UpdateMonitorHelper.finish"))
        }
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

    private var monitoringPlayerCache: ServerPlayerEntity? = null
    @JvmStatic
    fun monitorSetBlock(world: ServerWorld, pos: BlockPos, blockState: BlockState) {
        if (monitoringPlayerCache?.data()?.isRecording != true) {
            monitoringPlayerCache = world.server.playerManager.playerList.firstOrNull { it.data().isRecording }
        }
        if (isPlayerRecording()) {
            if (DEBUG_LOGGER.booleanValue) {
                MinecraftClient.getInstance().player?.sendMessage(Text.literal("set$pos, ${world.getBlockState(pos)} -> $blockState"))
            }
            monitoringPlayerCache!!.data().undo.lastOrNull()?.data?.computeIfAbsent(pos.asLong()) {
                PlayerData.Entry.fromWorld(world, pos)
            }
        }
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
            recordId,
            hashMapOf()
        )
        undoRecordsMap[recordId] = recording!!
        recordId++
        return recording!!
    }

    private fun removeRecord(id: Long) = undoRecordsMap.remove(id)
    @JvmStatic
    fun playerStartRecord(player: ServerPlayerEntity) {
        val playerView = player.data()
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
            playerView.redo.clear()
            if (playerView.undo.lastOrNull() != null) {
                if (playerView.undo.last().data.isEmpty()) {
                    removeRecord(playerView.undo.last().id)
                    playerView.undo.removeLast()
                }
            }
        }
    }

    @JvmStatic
    fun isPlayerRecording(): Boolean {
        return monitoringPlayerCache?.data()?.isRecording == true
    }
}