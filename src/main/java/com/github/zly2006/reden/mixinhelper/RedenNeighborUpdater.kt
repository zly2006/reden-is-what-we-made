package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.ServerData
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.block.*
import com.github.zly2006.reden.debugger.tree.TickStageTree
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.block.NeighborUpdater
import net.minecraft.world.block.ChainRestrictedNeighborUpdater as Updater119

class RedenNeighborUpdater(
    val world: ServerWorld,
    val serverData: ServerData,
) : NeighborUpdater {
    private var rootStage: BlockUpdateStage? = null

    override fun replaceWithStateForNeighborUpdate(direction: Direction, neighborState: BlockState, pos: BlockPos, neighborPos: BlockPos, flags: Int, maxUpdateDepth: Int) = wrapUpdate {
        with(StageBlockPPUpdate(it, Updater119.StateReplacementEntry(direction, neighborState, pos, neighborPos, flags, maxUpdateDepth))) {
            NeighborUpdater.replaceWithStateForNeighborUpdate(world, direction, neighborState, pos, neighborPos, flags, maxUpdateDepth)
        }
    }

    override fun updateNeighbor(pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos) = wrapUpdate {
        with(StageBlockNCUpdate(it, Updater119.SimpleEntry(pos, sourceBlock, sourcePos))) {
            val state = world.getBlockState(pos)
            NeighborUpdater.tryNeighborUpdate(world, state, pos, sourceBlock, sourcePos, false)
        }
    }

    override fun updateNeighbor(state: BlockState, pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos, movedByPiston: Boolean) = wrapUpdate {
        with(StageBlockNCUpdateWithSource(it, Updater119.StatefulEntry(state, pos, sourceBlock, sourcePos, movedByPiston))) {
            NeighborUpdater.tryNeighborUpdate(world, state, pos, sourceBlock, sourcePos, movedByPiston)
        }
    }

    override fun updateNeighbors(pos: BlockPos, sourceBlock: Block, except: Direction?) = wrapUpdate {
        with(StageBlockNCUpdateSixWay(it, Updater119.SixWayEntry(pos, sourceBlock, except))) {
            super.updateNeighbors(pos, sourceBlock, except)
        }
    }

    private fun wrapUpdate(action: TickStageTree.(TickStage /* parent */) -> Unit) {
        val rootInitialized = rootStage != null
        val tree = serverData.tickStageTree
        if (!rootInitialized) {
            rootStage = BlockUpdateStage(tree.activeStage)
            tree.push(rootStage!!)
        }
        tree.action(tree.activeStage!!)
        if (!rootInitialized) {
            rootStage = null
            tree.pop()
        }
    }
}
