package com.github.zly2006.reden.mixinhelper

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.block.ChainRestrictedNeighborUpdater
import net.minecraft.world.block.NeighborUpdater

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

    fun handle(entry: Entry): Boolean {
        return false
    }

    interface Entry {
        fun update(world: World): Boolean
    }


    data class PP(
        val direction: Direction,
        val neighborState: BlockState,
        val pos: BlockPos,
        val neighborPos: BlockPos,
        val updateFlags: Int
    ) :
        Entry {
        override fun update(world: World): Boolean {
            NeighborUpdater.replaceWithStateForNeighborUpdate(
                world,
                direction,
                neighborState,
                pos,
                neighborPos,
                updateFlags,
                512
            )
            return false
        }
    }

    data class NCLazy(
        val pos: BlockPos,
        val sourceBlock: Block,
        val sourcePos: BlockPos
    ) : Entry {
        override fun update(world: World): Boolean {
            val blockState = world.getBlockState(pos)
            NeighborUpdater.tryNeighborUpdate(world, blockState, pos, sourceBlock, sourcePos, false)
            return false
        }
    }


    data class NC(
        val state: BlockState,
        val pos: BlockPos,
        val sourceBlock: Block,
        val sourcePos: BlockPos,
        val movedByPiston: Boolean
    ) :
        Entry {
        override fun update(world: World): Boolean {
            NeighborUpdater.tryNeighborUpdate(world, state, pos, sourceBlock, sourcePos, movedByPiston)
            return false
        }
    }


    data class NC6W(
        private val pos: BlockPos,
        private val sourceBlock: Block,
        private val except: Direction?
    ) :
        Entry {
        private var currentDirectionIndex = 0

        init {
            if (NeighborUpdater.UPDATE_ORDER[currentDirectionIndex] == except) {
                ++currentDirectionIndex
            }
        }

        override fun update(world: World): Boolean {
            val blockPos = pos.offset(NeighborUpdater.UPDATE_ORDER[currentDirectionIndex++])
            val blockState = world.getBlockState(blockPos)
            @Suppress("DEPRECATION")
            blockState.neighborUpdate(world, blockPos, sourceBlock, pos, false)
            if (currentDirectionIndex < NeighborUpdater.UPDATE_ORDER.size && NeighborUpdater.UPDATE_ORDER[currentDirectionIndex] == except) {
                ++currentDirectionIndex
            }
            return currentDirectionIndex < NeighborUpdater.UPDATE_ORDER.size
        }
    }
}