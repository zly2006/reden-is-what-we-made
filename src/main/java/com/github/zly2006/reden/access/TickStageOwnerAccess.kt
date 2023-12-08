package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.TickStage

interface TickStageOwnerAccess {
    var tickStage: TickStage?
    var ticked: Boolean
}
