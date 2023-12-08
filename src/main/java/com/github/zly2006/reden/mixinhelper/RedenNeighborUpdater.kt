package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.ServerData
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.debugger.stages.block.BlockUpdateStage
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.block.NeighborUpdater

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
        }
        assert(stages.removeLast() == stage)
    }

    override fun replaceWithStateForNeighborUpdate(direction: Direction, neighborState: BlockState, pos: BlockPos, neighborPos: BlockPos, flags: Int, maxUpdateDepth: Int) {
        val initStage = initStage()
        initStage.children.forEach {
            it.yield()
        }
        resetStage(initStage)
    }

    override fun updateNeighbor(pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos) {
        TODO("Not yet implemented")
    }

    override fun updateNeighbor(state: BlockState, pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos, notify: Boolean) {
        TODO("Not yet implemented")
        state.properties
    }

    override fun updateNeighbors(pos: BlockPos, sourceBlock: Block, except: Direction?) {
        super.updateNeighbors(pos, sourceBlock, except)
    }
}
