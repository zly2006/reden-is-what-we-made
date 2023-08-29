@file:Suppress("HasPlatformType")

package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.malilib.options.RedenConfigBoolean
import com.github.zly2006.reden.malilib.options.RedenConfigBooleanHotkeyed
import com.github.zly2006.reden.malilib.options.RedenConfigHotkey
import com.github.zly2006.reden.malilib.options.RedenConfigInteger
import com.github.zly2006.reden.utils.isClient
import com.google.common.collect.ImmutableList
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.options.ConfigBase
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.config.options.ConfigStringList
import fi.dy.masa.malilib.hotkeys.IHotkey
import net.minecraft.client.MinecraftClient

private val loadingGuard = run {
    if (!isClient) {
        try {
            throw IllegalStateException("MalilibSettings must be loaded on client side.")
        } catch (e: IllegalStateException) {
            e.stackTraceToString()
            throw e
        }
    }
}

@JvmField val HOTKEYS = mutableListOf<IHotkey>()
@JvmField val GENERIC_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val RVC_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val MICRO_TICK_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val SUPER_RIGHT_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val DEBUG_TAB = mutableListOf<ConfigBase<*>>()
private fun <T : IHotkey> T.hotkey() = this.apply(HOTKEYS::add)
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.generic() = this.apply(GENERIC_TAB::add) as T
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.rvc() = this.apply(RVC_TAB::add) as T
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.mt() = this.apply(MICRO_TICK_TAB::add) as T
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.sr() = this.apply(SUPER_RIGHT_TAB::add) as T
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.debug() = this.apply(DEBUG_TAB::add) as T

@JvmField val REDEN_CONFIG_KEY = RedenConfigHotkey("redenConfigKey", "R,C").generic().hotkey()
@JvmField val ALLOW_COPYRIGHT_CHECK = RedenConfigBoolean("allowCopyrightCheck", true).generic()
@JvmField val SECURITY_COMMIT = RedenConfigBoolean("securityCommit", false).generic()
@JvmField val UNDO_KEY = RedenConfigHotkey("rollbackKey", "LEFT_CONTROL,Z").generic().hotkey()
@JvmField val REDO_KEY = RedenConfigHotkey("redoKey", "LEFT_CONTROL,Y").generic().hotkey()
@JvmField val UNDO_SUPPORT_LITEMATICA_OPERATION = RedenConfigBoolean("undoSupportLitematicaOperation", true).generic()
@JvmField val UNDO_CHEATING_ONLY = RedenConfigBoolean("undoCheatingOnly", true).generic()
@JvmField val MAX_RENDER_DISTANCE = RedenConfigInteger("maxRenderDistance", 48).generic()
@JvmField val TOGGLE_NC_BREAKPOINT = RedenConfigHotkey("toggleNcBreakpoint", "LEFT_CONTROL,LEFT_SHIFT,Z").mt().hotkey()
@JvmField val TOGGLE_PP_BREAKPOINT = RedenConfigHotkey("togglePpBreakpoint", "LEFT_CONTROL,LEFT_SHIFT,X").mt().hotkey()
@JvmField val TOGGLE_CU_BREAKPOINT = RedenConfigHotkey("toggleCuBreakpoint", "LEFT_CONTROL,LEFT_SHIFT,C").mt().hotkey()
@JvmField val TOGGLE_FORCE_ENTITY_POS_SYNC = RedenConfigBooleanHotkeyed("toggleForceEntityPosSync", false, "LEFT_CONTROL,LEFT_SHIFT,V").hotkey().mt()
@JvmField val RVC_RECORD_MULTIPLAYER = RedenConfigBoolean("rvcRecordMultiplayer", true).rvc()
@JvmField val RVC_FORCE_LOCALLY = RedenConfigBoolean("rvcForceLocally", false).rvc()
@JvmField val CHAT_RIGHT_CLICK_MENU = RedenConfigBoolean("chatRightClickMenu", true).sr()
@JvmField val DEBUG_LOGGER = RedenConfigBoolean("debugLogger", false).debug()
@JvmField val DEBUG_PACKET_LOGGER = RedenConfigBoolean("debugPacketLogger", false).debug()
@JvmField val DEBUG_TAG_BLOCK_POS = RedenConfigHotkey("debugTagBlockPos", "LEFT_CONTROL,LEFT_SHIFT,T").debug().hotkey()
@JvmField val DEBUG_PREVIEW_UNDO = RedenConfigHotkey("debugPreviewUndo", "LEFT_CONTROL,LEFT_SHIFT,Z").debug().hotkey()
@JvmField val MAX_CHAIN_UPDATES = RedenConfigInteger("maxChainUpdates", -1).debug()
@JvmField val DO_ASSERTION_CHECKS = RedenConfigBoolean("doAssertionChecks", false).debug()
@JvmField val UNDO_REPORT_UN_TRACKED_TNT = RedenConfigBoolean("undoReportUnTrackedTnt", false).debug()

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

fun createCommandHotkey(index: Int) {
    // We should find a better way to implement this. It should be refactored.
    val commands = ConfigStringList("command$index", ImmutableList.of(), "").sr()
    val hotkey = ConfigHotkey("runCommand$index", "", "Hotkey for executing command $index").hotkey().sr().runCommand(commands)
}

private val commands = (1..20).map(::createCommandHotkey)

fun getAllOptions() = GENERIC_TAB + RVC_TAB + MICRO_TICK_TAB + SUPER_RIGHT_TAB + DEBUG_TAB
