package com.github.zly2006.reden.access

interface UndoableAccess {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getUndoId\$reden")
    @set:JvmName("setUndoId\$reden")
    var undoId: Long
}
