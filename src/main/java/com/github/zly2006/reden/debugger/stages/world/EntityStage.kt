package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.entity.Entity

class EntityStage(
    val _parent: EntitiesRootStage,
    val entity: Entity
): TickStage("entity", _parent) {
}
