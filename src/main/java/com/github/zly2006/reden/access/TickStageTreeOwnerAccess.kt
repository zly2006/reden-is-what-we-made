package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.tree.TickStageTree

interface TickStageTreeOwnerAccess {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTickStageTree\$reden")
    @set:JvmName("setTickStageTree\$reden")
    var tickStageTree: TickStageTree?
}
