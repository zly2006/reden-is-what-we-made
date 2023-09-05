package com.github.zly2006.reden.malilib.gui.button

import com.github.zly2006.reden.malilib.gui.GuiCommandHotkeyListEdit
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui
import fi.dy.masa.malilib.hotkeys.IKeybind

class ConfigButtonKeybindInList(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    keybind: IKeybind,
    private val listGui: GuiCommandHotkeyListEdit
) : ConfigButtonKeybind(x, y, width, height, keybind, listGui.configGui as IKeybindConfigGui) {
    override fun onMouseClickedImpl(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        super.onMouseClickedImpl(mouseX, mouseY, mouseButton)
        if (selected) {
            listGui.activeKeybindButton = this
        }
        return true
    }

    override fun onClearSelection() {
        super.onClearSelection()
        listGui.activeKeybindButton = null
    }
}
