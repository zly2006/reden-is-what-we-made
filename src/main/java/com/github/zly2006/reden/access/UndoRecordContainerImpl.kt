package com.github.zly2006.reden.access

import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper

class UndoRecordContainerImpl : UndoRecordContainer {
    override var recording: PlayerData.UndoRecord? = null
    var id: Long
        get() = recording?.id ?: 0
        set(value) { recording = UpdateMonitorHelper.undoRecordsMap[value] }
}
