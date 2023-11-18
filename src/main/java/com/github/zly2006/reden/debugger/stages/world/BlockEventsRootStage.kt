package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.stages.WorldRootStage

class BlockEventsRootStage(
    parent: WorldRootStage
): AbstractWorldChildStage("block_events_root", parent) {
    override fun tick() {
        // don't clear children
        world.tick(_parent.shouldKeepTicking)
        yield()
    }
}
