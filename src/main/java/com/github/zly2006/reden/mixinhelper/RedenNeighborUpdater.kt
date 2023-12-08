package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.ServerData
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.block.*
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.block.NeighborUpdater
import net.minecraft.world.block.ChainRestrictedNeighborUpdater as Updater119

class RedenNeighborUpdater(
    val serverWorld: ServerWorld,
    val serverData: ServerData,
) : NeighborUpdater {
    private var rootStage: BlockUpdateStage? = null
    private val stages = mutableListOf<AbstractBlockUpdateStage<*>>()
    val isStageActive get() = rootStage != null
    val activeStage get() = rootStage!!

    /**
     * @return parent stage of the new stage
     */
    private fun initStage(): TickStage {
        val tree = serverData.tickStageTree
        if (rootStage == null) {
            rootStage = BlockUpdateStage(serverData.tickStageTree.peekLeaf())
            tree.insert2child(rootStage as BlockUpdateStage)
            return rootStage as BlockUpdateStage
        }
        return tree.peekLeaf()
    }

    private fun pushStage(stage: AbstractBlockUpdateStage<*>) {
        stages.add(stage)
    }

    private fun resetStage(stage: TickStage) {
        if (stage == rootStage) {
            rootStage = null
            stages.clear()
        } else {
            assert(stages.removeLast() == stage)
        }
    }

    override fun replaceWithStateForNeighborUpdate(direction: Direction, neighborState: BlockState, pos: BlockPos, neighborPos: BlockPos, flags: Int, maxUpdateDepth: Int) {
        val parent = initStage()
        pushStage(StageBlockPPUpdate(parent, Updater119.StateReplacementEntry(direction, neighborState, pos, neighborPos, flags, maxUpdateDepth - 1)))
        stages.last().tick()
        resetStage(parent)
    }

    override fun updateNeighbor(pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos) {
        val parent = initStage()
        pushStage(StageBlockNCUpdate(parent, Updater119.SimpleEntry(pos, sourceBlock, sourcePos)))
        stages.last().tick()
        resetStage(parent)
    }

    override fun updateNeighbor(state: BlockState, pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos, notify: Boolean) {
        val parent = initStage()
        pushStage(StageBlockNCUpdateWithSource(parent, Updater119.StatefulEntry(state, pos, sourceBlock, sourcePos, notify)))
        stages.last().tick()
        resetStage(parent)
    }

    override fun updateNeighbors(pos: BlockPos, sourceBlock: Block, except: Direction?) {
        super.updateNeighbors(pos, sourceBlock, except)
    }
}
