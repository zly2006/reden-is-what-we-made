package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage
import net.minecraft.world.spawner.Spawner

class SpecialSpawnStage(
    val _parent: WorldRootStage,
    val spawner: Spawner
): TickStage("special_spawn", _parent) {
}
