package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.TickStage

interface TickStageOwnerAccess {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTickStage\$reden")
    @set:JvmName("setTickStage\$reden")
    var tickStage: TickStage?

    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTicked\$reden")
    @set:JvmName("setTicked\$reden")
    var ticked: Boolean
}
