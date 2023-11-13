package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage
import net.minecraft.world.spawner.Spawner

class SpecialSpawnStage(
    val _parent: WorldRootStage,
    val spawner: Spawner
): TickStage("special_spawn", _parent) {
    override fun tick() {
        super.tick()
        spawner.spawn(_parent.world, _parent.world.chunkManager.spawnAnimals, _parent.world.chunkManager.spawnMonsters)
    }
}
