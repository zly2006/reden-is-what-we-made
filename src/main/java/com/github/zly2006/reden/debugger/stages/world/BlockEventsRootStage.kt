package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.stages.WorldRootStage

class BlockEventsRootStage(
    parent: WorldRootStage?
): AbstractWorldChildStage("block_events_root", parent) {
    // Note: tick() method that does not call TickStage#tick
    override fun tick() {
        // don't clear children
        world!!.tick(_parent!!.shouldKeepTicking)
        yield()
    }
}
