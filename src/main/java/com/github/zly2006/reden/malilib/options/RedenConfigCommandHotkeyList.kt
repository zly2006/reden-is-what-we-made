package com.github.zly2006.reden.malilib.options

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.malilib.data.CommandHotkey
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import fi.dy.masa.malilib.config.options.ConfigBase
import fi.dy.masa.malilib.hotkeys.KeybindMulti
import fi.dy.masa.malilib.hotkeys.KeybindSettings

class RedenConfigCommandHotkeyList(name: String) : IRedenConfigBase,
    ConfigBase<RedenConfigCommandHotkeyList>(
        null, name, REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX
    ) {

    val commandHotkeyList: MutableList<CommandHotkey> = mutableListOf()
    val defaultList: List<CommandHotkey> = listOf()

    var saveCallback: (() -> Unit)? = null

    override fun setValueFromJsonElement(element: JsonElement?) {
        commandHotkeyList.clear()

        try {
            for (item in element!!.asJsonArray) {
                if (item is JsonObject) {
                    val commands: MutableList<String> = mutableListOf()
                    for (cmd in item.get("commands").asJsonArray) {
                        commands.add(cmd.asString)
                    }
                    val hotkey = KeybindMulti.fromStorageString("", KeybindSettings.DEFAULT)
                    hotkey.setValueFromJsonElement(item.get("hotkey"))
                    commandHotkeyList.add(CommandHotkey(commands, hotkey))
                } else {
                    Reden.LOGGER.warn("Failed to read command hotkey data from config.")
                }
            }
        } catch (ignored: Error) {
            Reden.LOGGER.warn("Failed to read command hotkey data from config.")
        }
    }

    override fun getAsJsonElement(): JsonElement {
        if (saveCallback != null) {
            // We need this because `ConfigStringList` will never be saved, and we need to sync it
            saveCallback!!()
        }
        val array = JsonArray()
        for (commandHotkey in commandHotkeyList) {
            val obj = JsonObject()
            val commands = JsonArray()
            for (command in commandHotkey.commands) {
                commands.add(JsonPrimitive(command))
            }
            obj.add("commands", commands)
            obj.add("hotkey", commandHotkey.keybind.asJsonElement)
            array.add(obj)
        }
        return array
    }

    override fun isModified(): Boolean {
        return commandHotkeyList != defaultList
    }

    override fun resetToDefault() {
        commandHotkeyList.clear()
        commandHotkeyList.addAll(defaultList)
    }

}
