package com.github.zly2006.reden.mixinhelper

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.block.NeighborUpdater

/**
 * Implement for 1.18- neighbor updater
 */
class RecursiveUpdater(
    val world: ServerWorld
): NeighborUpdater {
    val helper = BreakpointHelper(world)
    override fun replaceWithStateForNeighborUpdate(
        direction: Direction,
        neighborState: BlockState,
        pos: BlockPos,
        neighborPos: BlockPos,
        flags: Int,
        maxUpdateDepth: Int
    ) {
        helper.handle(
            BreakpointHelper.PP(
                direction,
                neighborState,
                pos.toImmutable(),
                neighborPos.toImmutable(),
                flags
            )
        )
    }

    override fun updateNeighbor(pos: BlockPos, sourceBlock: Block, sourcePos: BlockPos) {
        helper.handle(BreakpointHelper.NCLazy(pos.toImmutable(), sourceBlock, sourcePos.toImmutable()))
    }

    override fun updateNeighbor(
        state: BlockState,
        pos: BlockPos,
        sourceBlock: Block,
        sourcePos: BlockPos,
        notify: Boolean
    ) {
        helper.handle(
            BreakpointHelper.NC(
                state,
                pos.toImmutable(),
                sourceBlock,
                sourcePos.toImmutable(),
                notify
            )
        )
    }
}
