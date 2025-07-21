@file:Suppress("HasPlatformType")
@file:Environment(EnvType.CLIENT)

package com.github.zly2006.reden.malilib

import fi.dy.masa.malilib.config.options.ConfigBase
import fi.dy.masa.malilib.config.options.ConfigBoolean
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.hotkeys.IHotkey
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

// @formatter:off
@JvmField val HOTKEYS = mutableListOf<IHotkey>()
@JvmField val GENERIC_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val DEBUG_TAB = mutableListOf<ConfigBase<*>>()
private fun <T : IHotkey> T.hotkey() = this.apply(HOTKEYS::add)
private fun <T : ConfigBase<*>> T.generic() = apply { (GENERIC_TAB.add(this)) }
private fun <T : ConfigBase<*>> T.debug() = apply { (DEBUG_TAB.add(this)) }

// Generic
@JvmField val REDEN_CONFIG_KEY = ConfigHotkey("redenConfigKey", "R,C", "reden.config.redenConfigKey.comment").generic().hotkey()
@JvmField val NO_TIME_OUT = ConfigBoolean("noTimeOut", false, "reden.config.noTimeOut.comment").generic()
@JvmField val UNDO_KEY = ConfigHotkey("undoKey", "LEFT_CONTROL,Z", "reden.config.undoKey.comment").generic().hotkey()
@JvmField val REDO_KEY = ConfigHotkey("redoKey", "LEFT_CONTROL,Y", "reden.config.redoKey.comment").generic().hotkey()
@JvmField val CHAT_RIGHT_CLICK_MENU = ConfigBoolean("chatRightClickMenu", true, "reden.config.chatRightClickMenu.comment", "reden.config.chatRightClickMenu", "reden.config.chatRightClickMenu").generic()
// Debug
@JvmField val DEBUG_LOGGING = ConfigBoolean("debugLogging", false).debug()
// @formatter:on

fun getAllOptions() = GENERIC_TAB + DEBUG_TAB
