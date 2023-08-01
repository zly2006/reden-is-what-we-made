package com.github.zly2006.reden.pearl

import fi.dy.masa.malilib.config.options.ConfigHotkey

class PearlSTask {
    enum class Mode(pearlState: Int) {
        NOT_INITIALIZED(0),
        RECORDING(0),
        NE(1),
        NW(2),
        SE(4),
        SW(8),
        PEARL(16),
        CALCULATING(-1),
        FINISHED(-1),
    }
    companion object {
        val masaHotkeyConfig = ConfigHotkey("pearlHotkey", "P,R", "")
        fun register() {
            masaHotkeyConfig.keybind.setCallback { action, iKeybind ->
                false
            }
        }
    }
}