package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.PlayerPatchesView
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
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
        listeners.forEach { (k, v) ->
            k.invoke(world, entry)
            if (v == LifeTime.ONCE) {
                listeners.remove(k)
            }
        }
    }

    @JvmStatic
    fun onChainFinish(world: World) {
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
        if (monitoringPlayerCache == null || !(monitoringPlayerCache as PlayerPatchesView).isRecording) {
            world.server.playerManager.playerList.stream()
                .filter { (it as PlayerPatchesView).isRecording }
                .findFirst().ifPresent { monitoringPlayerCache = it }
        }
        if (isPlayerRecording()) {
            (monitoringPlayerCache as PlayerPatchesView).blocks.last()[pos] = PlayerPatchesView.Entry(
                NbtHelper.fromBlockState(world.getBlockState(pos)),
                world.getBlockEntity(pos)?.createNbt()
            )
        }
    }

    @JvmStatic
    fun isPlayerRecording(): Boolean {
        return monitoringPlayerCache != null && (monitoringPlayerCache as PlayerPatchesView).isRecording
    }
}