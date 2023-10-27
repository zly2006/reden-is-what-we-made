package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage

/**
 * Used by client-side, as server may not sync all stage data to the client.
 */
class DummyStage(parent: TickStage?) : TickStage("dummy", parent, ) {
}