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
        val tree = serverData.stageTree
        val r = if (rootStage == null) {
            rootStage = BlockUpdateStage(serverData.stageTree.peekLeaf())
            tree.insert2child(rootStage as BlockUpdateStage)
            assert(tree.next() == rootStage) {
                "next() should return rootStage"
            } // add rootStage to tree
            rootStage as BlockUpdateStage
        } else stages.last()
        if (!tree.isInTree(r)) {
            error("Stage $r is not in tree")
        }
        return r
    }

    private fun pushStage(stage: AbstractBlockUpdateStage<*>) {
        stages.add(stage)
        serverData.stageTree.insert2childAtLast(stage.parent!!, stage)
    }

    private fun nextStage(): AbstractBlockUpdateStage<*> {
        val stage = stages.last()
        val next = serverData.stageTree.next()
        assert(next == stage)
        return stage
    }

    private fun popStage(stage: TickStage) {
        if (stage == rootStage) {
            rootStage = null
            println(stages.size)
            stages.clear()
        } else {
            assert(stages.removeLast() == stage)
        }
    }

    override fun replaceWithStateForNeighborUpdate(direction: Direction, neighborState: BlockState, pos: BlockPos, neighborPos: BlockPos, flags: Int, maxUpdateDepth: Int) {
        val parent = initStage()
        pushStage(StageBlockPPUpdate(parent, Updater119.StateReplacementEntry(direction, neighborState, pos, neighborPos, flags, maxUpdateDepth - 1)))
        nextStage().tick()
        popStage(parent)
    }

    override fun updateNeighbor(pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos) {
        val parent = initStage()
        pushStage(StageBlockNCUpdate(parent, Updater119.SimpleEntry(pos, sourceBlock, sourcePos)))
        nextStage().tick()
        popStage(parent)
    }

    override fun updateNeighbor(state: BlockState, pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos, notify: Boolean) {
        val parent = initStage()
        pushStage(StageBlockNCUpdateWithSource(parent, Updater119.StatefulEntry(state, pos, sourceBlock, sourcePos, notify)))
        nextStage().tick()
        popStage(parent)
    }

    override fun updateNeighbors(pos: BlockPos, sourceBlock: Block, except: Direction?) {
        val parent = initStage()
        pushStage(StageBlockNCUpdateSixWay(parent, Updater119.SixWayEntry(pos, sourceBlock, except)))
        nextStage().tick()
        popStage(parent)
    }
}
