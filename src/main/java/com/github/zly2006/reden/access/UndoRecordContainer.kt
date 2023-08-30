package com.github.zly2006.reden.access

interface UndoRecordContainer {
    var recording: PlayerData.UndoRecord?

    fun swap(another: UndoRecordContainer) {
        val tmp = recording
        recording = another.recording
        another.recording = tmp
    }
}
