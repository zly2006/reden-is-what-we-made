package com.redenmc.mineroutine

/**
 * Root frame for the tick stage.
 */
class TickFrame :Frame(
    RunStack(),
    "tick",
    null,
    ROOT_STAGE
) {
    companion object {
        val ROOT_STAGE = object: TickStage {

        }
    }
}
