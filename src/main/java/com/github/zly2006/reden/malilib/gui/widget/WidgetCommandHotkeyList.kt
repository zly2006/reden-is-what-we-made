package com.github.zly2006.reden.malilib.gui.widget

import com.github.zly2006.reden.malilib.data.CommandHotkey
import com.github.zly2006.reden.malilib.gui.GuiCommandHotkeyListEdit
import com.github.zly2006.reden.malilib.options.RedenConfigCommandHotkeyList
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase

class WidgetCommandHotkeyList(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    configWidth: Int,
    val parent: GuiCommandHotkeyListEdit
) : WidgetListConfigOptionsBase<CommandHotkey, WidgetCommandHotkeyListEntry>(x, y, width, height, configWidth) {

    val config: RedenConfigCommandHotkeyList = parent.config

    init {
        config.saveCallback = {
            for (widget in listWidgets) {
                widget.syncWithConfig()
            }
            InputEventHandler.getKeybindManager().updateUsedKeys()
        }
    }

    override fun getAllEntries(): MutableCollection<CommandHotkey> {
        return config.commandHotkeyList
    }

    override fun reCreateListEntryWidgets() {
        if (listContents.size == 0) {
            listWidgets.clear()
            maxVisibleBrowserEntries = 1

            val x = posX + 2
            val y = posY + 4 + browserEntriesOffsetY

            listWidgets.add(createListEntryWidget(x, y, -1, false, CommandHotkey.new()))
            scrollbar.maxValue = 0
        } else {
            super.reCreateListEntryWidgets()
        }
    }

    override fun createListEntryWidget(
        x: Int,
        y: Int,
        listIndex: Int,
        isOdd: Boolean,
        entry: CommandHotkey?
    ): WidgetCommandHotkeyListEntry {
        if (listIndex >= 0 && listIndex < config.commandHotkeyList.size) {
            val defaultValue =
                if (config.defaultList.size > listIndex) config.defaultList[listIndex] else CommandHotkey.new()
            return WidgetCommandHotkeyListEntry(
                x,
                y,
                browserEntryWidth,
                browserEntryHeight,
                listIndex,
                config.commandHotkeyList[listIndex],
                this,
                isOdd,
                defaultValue
            )
        } else {
            return WidgetCommandHotkeyListEntry(
                x,
                y,
                browserEntryWidth,
                browserEntryHeight,
                listIndex,
                CommandHotkey.new(),
                this,
                isOdd,
                CommandHotkey.new()
            )
        }
    }
}
