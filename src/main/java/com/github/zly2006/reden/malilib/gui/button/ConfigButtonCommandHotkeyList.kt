package com.github.zly2006.reden.malilib.gui.button

import com.github.zly2006.reden.malilib.gui.GuiCommandHotkeyListEdit
import com.github.zly2006.reden.malilib.options.RedenConfigCommandHotkeyList
import fi.dy.masa.malilib.gui.GuiBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.gui.interfaces.IConfigGui
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler
import fi.dy.masa.malilib.util.GuiUtils
import fi.dy.masa.malilib.util.StringUtils

class ConfigButtonCommandHotkeyList(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val config: RedenConfigCommandHotkeyList,
    private val configGui: IConfigGui,
    private val dialogHandler: IDialogHandler?
) : ButtonGeneric(x, y, width, height, "") {

    init {
        updateDisplayString()
    }

    override fun onMouseClickedImpl(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        super.onMouseClickedImpl(mouseX, mouseY, mouseButton)
        if (dialogHandler != null) {
            dialogHandler.openDialog(GuiCommandHotkeyListEdit(config, configGui, dialogHandler, null))
        } else {
            GuiBase.openGui(GuiCommandHotkeyListEdit(config, configGui, null, GuiUtils.getCurrentScreen()))
        }
        return true
    }

    override fun updateDisplayString() {
        displayString = StringUtils.translate("reden.widget.button.commandHotkeyListEdit")
    }
}
