@file:Suppress("HasPlatformType")

package com.github.zly2006.reden.malilib

import com.google.common.collect.ImmutableList
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.options.*
import net.minecraft.client.MinecraftClient

@JvmField val HOTKEYS = mutableListOf<ConfigHotkey>()
@JvmField val GENERIC_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val RVC_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val MICRO_TICK_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val DEBUG_TAB = mutableListOf<ConfigBase<*>>()
private fun <T : ConfigHotkey> T.hotkey() = this.apply(HOTKEYS::add)
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.generic() = this.apply(GENERIC_TAB::add) as T
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.rvc() = this.apply(RVC_TAB::add) as T
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.mt() = this.apply(MICRO_TICK_TAB::add) as T
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.debug() = this.apply(DEBUG_TAB::add) as T

val REDEN_CONFIG_KEY = ConfigHotkey("redenConfigKey", "R,C", "").generic().hotkey()
val ALLOW_COPYRIGHT_CHECK = ConfigBoolean("allowCopyrightCheck", true, "").generic()
val SECURITY_COMMIT = ConfigBoolean("securityCommit", false, "").generic()
val ROLLBACK_KEY = ConfigHotkey("rollbackKey", "LEFT_CONTROL,Z", "").generic().hotkey()
val REDO_KEY = ConfigHotkey("redoKey", "LEFT_CONTROL,Y", "").generic().hotkey()
val MAX_RENDER_DISTANCE = ConfigInteger("maxRenderDistance", 48, "").generic()
val TOGGLE_NC_BREAKPOINT = ConfigHotkey("toggleNcBreakpoint", "LEFT_CONTROL,LEFT_SHIFT,Z", "Neighbor Changed Event").mt().hotkey()
val TOGGLE_PP_BREAKPOINT = ConfigHotkey("togglePpBreakpoint", "LEFT_CONTROL,LEFT_SHIFT,X", "Post Placement Event").mt().hotkey()
val TOGGLE_CU_BREAKPOINT = ConfigHotkey("toggleCuBreakpoint", "LEFT_CONTROL,LEFT_SHIFT,C", "Comparator Update Event").mt().hotkey()
val RVC_RECORD_MULTIPLAYER = ConfigBoolean("rvcRecordMultiplayer", true, "Enable RVC in multiplayer").rvc()
val RVC_FORCE_LOCALLY = ConfigBoolean("rvcForceLocally", false, "Force RVC to run locally even the remote server enabled RVC").rvc()
val DEBUG_LOGGER = ConfigBoolean("debugLogger", false, "Enable debug logger").debug()
val DEBUG_TAG_BLOCK_POS = ConfigHotkey("debugTagBlockPos", "LEFT_CONTROL,LEFT_SHIFT,T", "Tag block position").debug().hotkey()
val DEBUG_PREVIEW_UNDO = ConfigHotkey("debugPreviewUndo", "LEFT_CONTROL,LEFT_SHIFT,Z", "Preview undo").debug().hotkey()
val MAX_CHAIN_UPDATES = ConfigInteger("maxChainUpdates", -1, "Max chain updates, affects after reopening").debug()
fun ConfigHotkey.runCommand(commands: ConfigStringList) {
    this.keybind.setCallback { _, _ ->
        val net = MinecraftClient.getInstance().networkHandler!!
        commands.strings.forEach {
            if (it.startsWith('/')) net.sendChatCommand(it.substring(1))
            else net.sendChatMessage(it)
        }
        true
    }
}

@JvmField val COMMAND_1 = ConfigStringList("command1", ImmutableList.of(), "").debug()
@JvmField val RUN_COMMAND_1 = ConfigHotkey("runCommand1", "", "Hotkey for executing command 1").hotkey().debug().runCommand(COMMAND_1)
@JvmField val COMMAND_2 = ConfigStringList("command2", ImmutableList.of(), "").debug()
@JvmField val RUN_COMMAND_2 = ConfigHotkey("runCommand2", "", "Hotkey for executing command 2").hotkey().debug().runCommand(COMMAND_2)
@JvmField val COMMAND_3 = ConfigStringList("command3", ImmutableList.of(), "").debug()
@JvmField val RUN_COMMAND_3 = ConfigHotkey("runCommand3", "", "Hotkey for executing command 3").hotkey().debug().runCommand(COMMAND_3)
@JvmField val COMMAND_4 = ConfigStringList("command4", ImmutableList.of(), "").debug()
@JvmField val RUN_COMMAND_4 = ConfigHotkey("runCommand4", "", "Hotkey for executing command 4").hotkey().debug().runCommand(COMMAND_4)
@JvmField val COMMAND_5 = ConfigStringList("command5", ImmutableList.of(), "").debug()
@JvmField val RUN_COMMAND_5 = ConfigHotkey("runCommand5", "", "Hotkey for executing command 5").hotkey().debug().runCommand(COMMAND_5)
@JvmField val COMMAND_6 = ConfigStringList("command6", ImmutableList.of(), "").debug()
@JvmField val RUN_COMMAND_6 = ConfigHotkey("runCommand6", "", "Hotkey for executing command 6").hotkey().debug().runCommand(COMMAND_6)
@JvmField val COMMAND_7 = ConfigStringList("command7", ImmutableList.of(), "").debug()
@JvmField val RUN_COMMAND_7 = ConfigHotkey("runCommand7", "", "Hotkey for executing command 7").hotkey().debug().runCommand(COMMAND_7)
@JvmField val COMMAND_8 = ConfigStringList("command8", ImmutableList.of(), "").debug()
@JvmField val RUN_COMMAND_8 = ConfigHotkey("runCommand8", "", "Hotkey for executing command 8").hotkey().debug().runCommand(COMMAND_8)
@JvmField val COMMAND_9 = ConfigStringList("command9", ImmutableList.of(), "").debug()
@JvmField val RUN_COMMAND_9 = ConfigHotkey("runCommand9", "", "Hotkey for executing command 9").hotkey().debug().runCommand(COMMAND_9)

fun debug() = DEBUG_LOGGER.booleanValue

fun getAllOptions() = GENERIC_TAB + RVC_TAB + MICRO_TICK_TAB + DEBUG_TAB

fun configScreen(): GuiConfigs {
    return GuiConfigs()
}