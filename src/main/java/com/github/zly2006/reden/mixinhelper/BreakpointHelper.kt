package com.github.zly2006.reden.mixinhelper

import net.minecraft.world.World
import net.minecraft.world.block.ChainRestrictedNeighborUpdater

class BreakpointHelper(
    val world: World
) {
    private val listeners: MutableMap<World.(ChainRestrictedNeighborUpdater.Entry) -> Unit, UpdateMonitorHelper.LifeTime> = mutableMapOf()
    private val chainFinishListeners = mutableMapOf<World.() -> Unit, UpdateMonitorHelper.LifeTime>()
    var isInterrupted = false
        private set

    fun onChainFinish() {
        //debugLogger("UpdateMonitorHelper.finish")
        listeners.forEach { (k, v) ->
            if (v == UpdateMonitorHelper.LifeTime.CHAIN) {
                listeners.remove(k)
            }
        }
        chainFinishListeners.forEach { (k, v) ->
            k.invoke(world)
            if (v == UpdateMonitorHelper.LifeTime.ONCE || v == UpdateMonitorHelper.LifeTime.CHAIN) {
                chainFinishListeners.remove(k)
            }
        }
    }

    fun startMonitor(onUpdate: World.(ChainRestrictedNeighborUpdater.Entry) -> Unit, lifeTime: UpdateMonitorHelper.LifeTime) {
        listeners[onUpdate] = lifeTime
    }

    fun onUpdate(entry: ChainRestrictedNeighborUpdater.Entry) {
        //debugLogger("UpdateMonitorHelper.onUpdate")
        listeners.forEach { (k, v) ->
            k.invoke(world, entry)
            if (v == UpdateMonitorHelper.LifeTime.ONCE) {
                listeners.remove(k)
            }
        }
    }
}