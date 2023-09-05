package com.github.zly2006.reden.malilib.gui

import com.github.zly2006.reden.malilib.data.CommandHotkey
import com.github.zly2006.reden.malilib.gui.widget.WidgetCommandHotkeyList
import com.github.zly2006.reden.malilib.gui.widget.WidgetCommandHotkeyListEntry
import com.github.zly2006.reden.malilib.options.RedenConfigCommandHotkeyList
import fi.dy.masa.malilib.config.ConfigManager
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.gui.GuiListBase
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind
import fi.dy.masa.malilib.gui.interfaces.IConfigGui
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler
import fi.dy.masa.malilib.render.RenderUtils
import fi.dy.masa.malilib.util.GuiUtils
import fi.dy.masa.malilib.util.KeyCodes
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen

class GuiCommandHotkeyListEdit(
    val config: RedenConfigCommandHotkeyList,
    val configGui: IConfigGui,
    val dialogHandler: IDialogHandler?,
    parent: Screen?
) : GuiListBase<CommandHotkey, WidgetCommandHotkeyListEntry, WidgetCommandHotkeyList>(0, 0) {

    private var dialogWidth: Int = 0
    private var dialogHeight: Int = 0
    private var dialogLeft: Int = 0
    private var dialogTop: Int = 0

    var activeKeybindButton: ConfigButtonKeybind? = null

    init {
        title = StringUtils.translate("reden.widget.title.commandHotkeyListEdit")
        if (dialogHandler == null) {
            setParent(parent)
        }
    }

    private fun setWidthAndHeight() {
        dialogWidth = 400
        dialogHeight = GuiUtils.getScaledWindowHeight() - 90
    }

    private fun centerOnScreen() {
        if (parent != null) {
            val parent = parent!!
            dialogLeft = parent.width / 2 - dialogWidth / 2
            dialogTop = parent.height / 2 - dialogHeight / 2
        } else {
            dialogLeft = 20
            dialogTop = 20
        }
    }

    override fun initGui() {
        setWidthAndHeight()
        centerOnScreen()
        reCreateListWidget()
        super.initGui()
    }

    override fun removed() {
        if (listWidget!!.wereConfigsModified()) {
            listWidget!!.applyPendingModifications()
            ConfigManager.getInstance().onConfigsChanged(configGui.modId)
            InputEventHandler.getKeybindManager().updateUsedKeys()
        }
        super.removed()
    }

    override fun createListWidget(listX: Int, listY: Int): WidgetCommandHotkeyList {
        return WidgetCommandHotkeyList(
            dialogLeft + 10,
            dialogTop + 20,
            browserWidth,
            browserHeight,
            dialogWidth - 100,
            this
        )
    }

    override fun getBrowserWidth(): Int {
        return dialogWidth - 14
    }

    override fun getBrowserHeight(): Int {
        return dialogHeight - 30
    }

    override fun render(drawContext: DrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (parent != null) {
            parent!!.render(drawContext, mouseX, mouseY, partialTicks)
        }

        super.render(drawContext, mouseX, mouseY, partialTicks)
    }

    override fun drawScreenBackground(mouseX: Int, mouseY: Int) {
        RenderUtils.drawOutlinedBox(
            dialogLeft, dialogTop, dialogWidth, dialogHeight,
            0xFF000000.toInt(),
            0xFF999999.toInt()
        )
    }

    override fun drawTitle(drawContext: DrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawStringWithShadow(drawContext, title, dialogLeft + 10, dialogTop + 6, 0xFFFFFFFF.toInt())
    }

    override fun onKeyTyped(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (activeKeybindButton != null) {
            if (activeKeybindButton!!.isSelected) {
                activeKeybindButton!!.onKeyPressed(keyCode)
                return true
            }
        }

        return if (keyCode == KeyCodes.KEY_ESCAPE && dialogHandler != null) {
            dialogHandler.closeDialog()
            true
        } else {
            super.onKeyTyped(keyCode, scanCode, modifiers)
        }
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (super.onMouseClicked(mouseX, mouseY, mouseButton)) {
            return true
        }

        if (activeKeybindButton != null) {
            activeKeybindButton!!.onClearSelection()
            activeKeybindButton = null
            return true
        }

        return false
    }
}
