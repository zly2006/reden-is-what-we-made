package com.github.zly2006.reden.mixinhelper

import com.github.zly2006.reden.access.ServerData
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
    private var stage: BlockUpdateStage? = null
    val isStageActive get() = stage != null
    val activeStage get() = stage!!

    override fun replaceWithStateForNeighborUpdate(direction: Direction, neighborState: BlockState, pos: BlockPos, neighborPos: BlockPos, flags: Int, maxUpdateDepth: Int) {
        val resetStage = stage == null
        if (resetStage) {
            stage = BlockUpdateStage(serverData.tickStageTree.peekLeaf())
        }
        TODO()
    }

    override fun updateNeighbor(pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos) {
        TODO("Not yet implemented")
    }

    override fun updateNeighbor(state: BlockState, pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos, notify: Boolean) {
        TODO("Not yet implemented")
    }

    override fun updateNeighbors(pos: BlockPos, sourceBlock: Block, except: Direction?) {
        super.updateNeighbors(pos, sourceBlock, except)
    }
}
