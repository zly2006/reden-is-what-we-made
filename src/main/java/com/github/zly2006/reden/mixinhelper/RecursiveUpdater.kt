package com.github.zly2006.reden.mixinhelper

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.block.NeighborUpdater
import java.util.*

class RecursiveUpdater(
    val world: ServerWorld
): NeighborUpdater {

    fun onEmitPP(pos: BlockPos) {

    }
    fun onEmitNC(pos: BlockPos) {

    }
    fun onEmitCU(pos: BlockPos) {

    }
    fun onRecvPP(pos: BlockPos) {

    }
    fun onRecvNC(pos: BlockPos) {

    }
    fun onRecvCU(pos: BlockPos) {

    }
    val pending = Stack<Runnable>()
    override fun replaceWithStateForNeighborUpdate(
        direction: Direction?,
        neighborState: BlockState?,
        pos: BlockPos?,
        neighborPos: BlockPos?,
        flags: Int,
        maxUpdateDepth: Int
    ) {
        onEmitPP(pos!!)
        onRecvPP(neighborPos!!)
        pending.add {
            NeighborUpdater.replaceWithStateForNeighborUpdate(
                world,
                direction,
                neighborState,
                pos,
                neighborPos,
                flags,
                maxUpdateDepth
            )
        }
    }

    override fun updateNeighbor(pos: BlockPos?, sourceBlock: Block?, sourcePos: BlockPos?) {
        val blockState = this.world.getBlockState(pos)
        this.updateNeighbor(blockState, pos, sourceBlock, sourcePos, false)
    }

    override fun updateNeighbor(
        state: BlockState?,
        pos: BlockPos?,
        sourceBlock: Block?,
        sourcePos: BlockPos?,
        notify: Boolean
    ) {
        onEmitNC(pos!!)
        NeighborUpdater.tryNeighborUpdate(world, state, pos, sourceBlock, sourcePos, notify)
    }
}
