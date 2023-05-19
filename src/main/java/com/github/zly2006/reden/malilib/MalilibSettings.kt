@file:Suppress("HasPlatformType")

package com.github.zly2006.reden.malilib

import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.options.ConfigBase
import fi.dy.masa.malilib.config.options.ConfigBoolean
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.config.options.ConfigInteger

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
val PREVIEW_UNDO = ConfigHotkey("previewUndo", "LEFT_CONTROL,LEFT_SHIFT,Z", "Preview undo").debug().hotkey()
val MAX_CHAIN_UPDATES = ConfigInteger("maxChainUpdates", -1, "Max chain updates, affects after reopening").debug()

fun debug() = DEBUG_LOGGER.booleanValue

fun getAllOptions() = GENERIC_TAB + RVC_TAB + MICRO_TICK_TAB + DEBUG_TAB

fun configScreen(): GuiConfigs {
    return GuiConfigs()
}