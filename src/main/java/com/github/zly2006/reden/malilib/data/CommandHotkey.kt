package com.github.zly2006.reden.malilib.data

import fi.dy.masa.malilib.hotkeys.IKeybind
import fi.dy.masa.malilib.hotkeys.KeybindMulti
import fi.dy.masa.malilib.hotkeys.KeybindSettings

data class CommandHotkey(
    val commands: MutableList<String>,
    val keybind: IKeybind
) {
    companion object {
        fun new(): CommandHotkey {
            return CommandHotkey(mutableListOf(), KeybindMulti.fromStorageString("", KeybindSettings.DEFAULT))
        }
    }
}
