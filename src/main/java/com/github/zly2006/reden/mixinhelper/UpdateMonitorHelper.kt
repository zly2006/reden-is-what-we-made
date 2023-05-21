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
    val listeners: MutableMap<World.(ChainRestrictedNeighborUpdater.Entry) -> Unit, LifeTime> = mutableMapOf()
    val chainFinishListeners = mutableMapOf<World.() -> Unit, LifeTime>()
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
            world.server.playerManager.playerList.stream()
                .filter { it.data().isRecording }
                .findFirst().ifPresent { monitoringPlayerCache = it }
        }
        if (isPlayerRecording()) {
            if (DEBUG_LOGGER.booleanValue) {
                MinecraftClient.getInstance().player?.sendMessage(Text.literal("set$pos, ${world.getBlockState(pos)} -> $blockState"))
            }
            monitoringPlayerCache!!.data().undo.lastOrNull()?.computeIfAbsent(pos.asLong()) {
                PlayerData.Entry.fromWorld(world, pos)
            }
        }
    }

    @JvmStatic
    fun isPlayerRecording(): Boolean {
        return monitoringPlayerCache?.data()?.isRecording == true
    }
}