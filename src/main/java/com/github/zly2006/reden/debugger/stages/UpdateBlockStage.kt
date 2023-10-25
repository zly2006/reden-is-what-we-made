package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld

class UpdateBlockStage(
    parent: TickStage?,
): TickStage("update_block", parent), TickStageWithWorld {
    override val world = (parent as TickStageWithWorld).world
    val updater = world?.neighborUpdater
    override fun tick() {

    }

    override fun toString() = "UpdateBlockStage/${world?.registryKey?.value}"
}
