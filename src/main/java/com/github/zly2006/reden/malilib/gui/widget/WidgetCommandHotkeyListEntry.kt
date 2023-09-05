package com.github.zly2006.reden.malilib.gui.widget

import com.github.zly2006.reden.malilib.data.CommandHotkey
import com.github.zly2006.reden.malilib.gui.button.ConfigButtonKeybindInList
import com.github.zly2006.reden.malilib.options.RedenConfigStringList
import com.google.common.collect.ImmutableList
import fi.dy.masa.malilib.gui.MaLiLibIcons
import fi.dy.masa.malilib.gui.button.ButtonBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.gui.button.ConfigButtonStringList
import fi.dy.masa.malilib.gui.button.IButtonActionListener
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings
import fi.dy.masa.malilib.hotkeys.IKeybind
import fi.dy.masa.malilib.render.RenderUtils
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.gui.DrawContext

class WidgetCommandHotkeyListEntry(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    listIndex: Int,
    initialValue: CommandHotkey,
    parent: WidgetCommandHotkeyList,
    private val isOdd: Boolean,
    private val defaultValue: CommandHotkey
) : WidgetConfigOptionBase<CommandHotkey>(x, y, width, height, parent, initialValue, listIndex) {

    private val config = parent.config.commandHotkeyList
    private val name = parent.config.name
    private val listGui = parent.parent
    private val configGui = listGui.configGui
    private val dialogHandler = listGui.dialogHandler
    private val initialValue = initialValue.copy()
    private val isDummy: Boolean
        get() = listIndex < 0

    private lateinit var resetButton: ButtonGeneric
    private lateinit var commandListConfig: RedenConfigStringList
    private lateinit var commandListButton: ConfigButtonStringList
    private lateinit var keybind: IKeybind
    private lateinit var keybindButton: ConfigButtonKeybindInList

    init {
        val commandListX = x + 20
        val commandListWidth = 100
        val keybindX = commandListX + commandListWidth + 2
        val keybindWidth = 100
        val keybindSettingsX = keybindX + keybindWidth + 2
        val keybindSettingsWidth = 20
        val resetX = keybindSettingsX + keybindSettingsWidth + 2
        val by = y + 4
        val bOff = 18

        if (isDummy) {
            addListActionButton(commandListX, by, ButtonType.ADD)
        } else {
            addLabel(x + 2, y + 6, 20, 12, 0xC0C0C0C0.toInt(), String.format("%3d:", listIndex + 1))
            var bx = addButtons(commandListX, keybindX, keybindSettingsX, resetX, y)

            addListActionButton(bx, by, ButtonType.ADD)
            bx += bOff

            addListActionButton(bx, by, ButtonType.REMOVE)
            bx += bOff

            if (canBeMoved(true)) {
                addListActionButton(bx, by, ButtonType.MOVE_DOWN)
            }

            bx += bOff

            if (canBeMoved(false)) {
                addListActionButton(bx, by, ButtonType.MOVE_UP)
            }

        }
    }

    fun syncWithConfig() {
        // Manually copy all commands from config.
        // The hotkey doesn't need this because it's passed by reference
        if (!isDummy) {
            config[listIndex].commands.clear()
            config[listIndex].commands.addAll(commandListConfig.strings)
        }
    }

    private fun addListActionButton(x: Int, y: Int, type: ButtonType) {
        val button = ButtonGeneric(x, y, type.icon, type.getDisplayName())
        val listener = ListenerListActions(type, this)
        addButton(button, listener)
    }

    private fun insertEntryBefore() {
        val size = config.size
        val index = if (listIndex < 0 || listIndex >= size) size else listIndex
        config.add(index, CommandHotkey.new())
        parent.refreshEntries()
        parent.markConfigsModified()
    }

    private fun removeEntry() {
        val size = config.size
        if (listIndex in 0 until size) {
            keybindButton.onClearSelection()
            config.removeAt(listIndex)
            parent.refreshEntries()
            parent.markConfigsModified()
        }
    }

    private fun moveEntry(down: Boolean) {
        val size = config.size
        if (listIndex in 0 until size) {
            var toIndex = -1
            if (down && listIndex < size - 1) {
                toIndex = listIndex + 1
            } else if (!down && listIndex > 0) {
                toIndex = listIndex - 1
            }

            if (toIndex >= 0) {
                parent.markConfigsModified()
                parent.applyPendingModifications()
                val tmp = config[listIndex]
                config[listIndex] = config[toIndex]
                config[toIndex] = tmp
                parent.refreshEntries()
            }
        }
    }

    private fun canBeMoved(down: Boolean): Boolean {
        val size = config.size
        return if (down) {
            listIndex in 0 until size && listIndex < size - 1
        } else {
            listIndex in 0 until size && listIndex > 0
        }
    }

    private fun addButtons(commandListX: Int, keybindX: Int, keybindSettingsX: Int, resetX: Int, y: Int): Int {
        val labelReset = StringUtils.translate("malilib.gui.button.reset.caps")
        resetButton = ButtonGeneric(resetX, y, -1, 20, labelReset)

        // This will never be saved because `getAsJsonElement` will never be called
        commandListConfig = RedenConfigStringList("commandList", ImmutableList.copyOf(defaultValue.commands))
        val listenerReset = ListenerResetConfig(this)

        commandListConfig.strings = config[listIndex].commands
        commandListButton =
            ConfigButtonStringList(commandListX, y, 100, 20, commandListConfig, configGui, dialogHandler)

        keybind = config[listIndex].keybind
        keybindButton = ConfigButtonKeybindInList(keybindX, y, 100, 20, keybind, listGui)

        val keybindSettings = WidgetKeybindSettings(keybindSettingsX, y, 20, 20, keybind, name, parent, dialogHandler)

        addButton(commandListButton, configGui.buttonPressListener)
        addButton(keybindButton, configGui.buttonPressListener)
        addWidget(keybindSettings)
        addButton(resetButton, listenerReset)

        return resetButton.x + resetButton.width + 4
    }

    override fun wasConfigModified(): Boolean {
        return !isDummy && config[listIndex] != initialValue
    }

    override fun applyNewValueToConfig() {
        // Config was already updated in `syncWithConfig`, so nothing needs to be done here
    }

    override fun render(mouseX: Int, mouseY: Int, selected: Boolean, drawContext: DrawContext?) {
        RenderUtils.color(1.0F, 1.0F, 1.0F, 1.0F)
        if (isOdd) {
            RenderUtils.drawRect(x, y, width, height, 0x20FFFFFF)
        } else {
            RenderUtils.drawRect(x, y, width, height, 0x30FFFFFF)
        }

        super.render(mouseX, mouseY, selected, drawContext)
    }

    private class ListenerListActions(private val type: ButtonType, private val parent: WidgetCommandHotkeyListEntry) :
        IButtonActionListener {
        override fun actionPerformedWithButton(button: ButtonBase?, mouseButton: Int) {
            when (this.type) {
                ButtonType.ADD -> parent.insertEntryBefore()
                ButtonType.REMOVE -> parent.removeEntry()
                ButtonType.MOVE_DOWN -> parent.moveEntry(true)
                ButtonType.MOVE_UP -> parent.moveEntry(false)
            }
        }

    }

    private class ListenerResetConfig(private val parent: WidgetCommandHotkeyListEntry) :
        IButtonActionListener {
        override fun actionPerformedWithButton(button: ButtonBase?, mouseButton: Int) {
            parent.commandListConfig.resetToDefault()
            parent.commandListButton.updateDisplayString()
            parent.keybind.resetToDefault()
            parent.keybindButton.updateDisplayString()
        }

    }

    private enum class ButtonType(val icon: MaLiLibIcons, val hoverTextKey: String) {
        ADD(MaLiLibIcons.PLUS, "malilib.gui.button.hovertext.add"),
        REMOVE(MaLiLibIcons.MINUS, "malilib.gui.button.hovertext.remove"),
        MOVE_UP(MaLiLibIcons.ARROW_UP, "malilib.gui.button.hovertext.move_up"),
        MOVE_DOWN(MaLiLibIcons.ARROW_DOWN, "malilib.gui.button.hovertext.move_down");

        fun getDisplayName(): String {
            return StringUtils.translate(hoverTextKey)
        }
    }
}
