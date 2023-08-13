package com.github.zly2006.reden.carpet

import carpet.api.settings.Rule

const val CATEGORY_REDEN = "Reden"

object CarpetSettings {
    /**
     * In 1.18-, the stack overflow error will be thrown when there are too many updates in one tick.
     * However, we have to replace the recursive method with a loop to use breakpoint features.
     * This is the of updates in one tick,
     * when this number is reached and the game is 1.18-, we throw a stack overflow error.
     */
    @Rule(
        categories = [CATEGORY_REDEN],
        options = ["-1", "500000"],
    )
    val stackOverflowUpdates = 500000

    @Rule(
        categories = [CATEGORY_REDEN],
        options = ["52428800"], // 50 MB
    )
    val allowedUndoSizeInBytes = 52428800
}
