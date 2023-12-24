@file:Suppress("HasPlatformType")

package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.malilib.options.*
import com.github.zly2006.reden.render.SolidFaceRenderer.ShapePredicateOptionEntry
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.startDebugAppender
import com.github.zly2006.reden.utils.stopDebugAppender
import fi.dy.masa.malilib.config.HudAlignment
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.options.ConfigBase
import fi.dy.masa.malilib.config.options.ConfigOptionList
import fi.dy.masa.malilib.hotkeys.IHotkey
import fi.dy.masa.malilib.hotkeys.KeybindSettings

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
@JvmField val HIDDEN_TAB = mutableListOf<ConfigBase<*>>()
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
@Suppress("UNCHECKED_CAST")
private fun <T : IConfigBase?> ConfigBase<T>.hidden() = this.apply(HIDDEN_TAB::add) as T

// Generic
@JvmField val REDEN_CONFIG_KEY = RedenConfigHotkey("redenConfigKey", "R,C").generic().hotkey()
@JvmField val SELECTION_TOOL = RedenConfigString("selectionTool", "minecraft:blaze_rod").generic()
@JvmField val HUD_POSITION = ConfigOptionList("hudPosition", HudAlignment.BOTTOM_LEFT, "").generic()
@JvmField val NO_TIME_OUT = RedenConfigBoolean("noTimeOut", false).generic()
@JvmField val BLOCK_BORDER_ALPHA = RedenConfigFloat("blockBorderAlpha", 0.1f, 0f, 1f).generic()
@JvmField val UNDO_KEY = RedenConfigHotkey("rollbackKey", "LEFT_CONTROL,Z").generic().hotkey()
@JvmField val REDO_KEY = RedenConfigHotkey("redoKey", "LEFT_CONTROL,Y").generic().hotkey()
@JvmField val UNDO_SUPPORT_LITEMATICA_OPERATION = RedenConfigBoolean("undoSupportLitematicaOperation", true).generic()
@JvmField val UNDO_CHEATING_ONLY = RedenConfigBoolean("undoCheatingOnly", true).generic()
@JvmField val MAX_RENDER_DISTANCE = RedenConfigInteger("maxRenderDistance", 48).generic()
@JvmField val SOLID_FACE_RENDERER = RedenConfigBoolean("solidFaceRenderer", false).generic()
@JvmField val ENABLE_CLIENT_GLOW = RedenConfigBooleanHotkeyed("enableClientGlow", true, "LEFT_CONTROL,G").hotkey().generic()
@JvmField val SOLID_FACE_SHAPE_PREDICATE = RedenConfigOptionList("solidFaceShapePredicate", ShapePredicateOptionEntry.FULL).generic()
@JvmField val EASTER_EGG_RATE = RedenConfigInteger("easterEggRate", 3, 0, 100).generic()
// Micro Tick
@JvmField val ADD_BREAKPOINT = RedenConfigHotkey("addBreakpoint", "LEFT_SHIFT,B").mt().hotkey()
@JvmField val CYCLE_BREAKPOINT_TYPE = RedenConfigHotkey("cycleBreakpointType", "LEFT_SHIFT,T").mt().hotkey()
@JvmField val EDIT_BREAKPOINTS = RedenConfigHotkey("editBreakpoints", "LEFT_CONTROL,B").mt().hotkey()
@JvmField val VIEW_ALL_BREAKPOINTS = RedenConfigHotkey("viewAllBreakpoints", "RIGHT_SHIFT,SPACE").mt().hotkey()
@JvmField val PAUSE_KEY = RedenConfigHotkey("pauseKey", "LEFT_CONTROL,LEFT_SHIFT,P").mt().hotkey()
@JvmField val CONTINUE_KEY = RedenConfigHotkey("continueKey", "").mt().hotkey()
@JvmField val STEP_INTO_KEY = RedenConfigHotkey("stepIntoKey", "").mt().hotkey()
@JvmField val STEP_OVER_KEY = RedenConfigHotkey("stepOverKey", "").mt().hotkey()
@JvmField val TOGGLE_FORCE_ENTITY_POS_SYNC = RedenConfigBooleanHotkeyed("toggleForceEntityPosSync", false, "LEFT_CONTROL,LEFT_SHIFT,V").hotkey().mt()
// RVC
@JvmField val OPEN_RVC_SCREEN = RedenConfigHotkey("openRvcScreen", "R", KeybindSettings.RELEASE).rvc().hotkey()
@JvmField val OPEN_SELECTION_LIST = RedenConfigHotkey("openSelectionList", "R,L").rvc().hotkey()
@JvmField val RVC_RECORD_MULTIPLAYER = RedenConfigBoolean("rvcRecordMultiplayer", true).rvc()
@JvmField val RVC_FORCE_LOCALLY = RedenConfigBoolean("rvcForceLocally", false).rvc()
// Super Right
@JvmField val CHAT_RIGHT_CLICK_MENU = RedenConfigBoolean("chatRightClickMenu", true).sr()
@JvmField val STRUCTURE_BLOCK_LOAD = RedenConfigHotkey("structureBlockLoad", "LEFT_CONTROL,L").sr().hotkey()
@JvmField val STRUCTURE_BLOCK_SAVE = RedenConfigHotkey("structureBlockSave", "LEFT_CONTROL,S").sr().hotkey()
@JvmField val RUN_COMMAND = RedenConfigCommandHotkeyList("runCommand").sr()
// Debug
@JvmField val DEBUG_LOGGER = RedenConfigBoolean("debugLogger", false) {
    if (booleanValue) startDebugAppender()
    else stopDebugAppender()
}.debug()
@JvmField val DEBUG_PACKET_LOGGER = RedenConfigBoolean("debugPacketLogger", false).debug()
@JvmField val DEBUG_TAG_BLOCK_POS = RedenConfigHotkey("debugTagBlockPos", "LEFT_CONTROL,LEFT_SHIFT,T").debug().hotkey()
@JvmField val DEBUG_LOGGER_IGNORE_UNDO_ID_0 = RedenConfigBoolean("debugLoggerIgnoreUndoId0", false).debug()
@JvmField val DEBUG_PREVIEW_UNDO = RedenConfigHotkey("debugPreviewUndo", "LEFT_CONTROL,LEFT_SHIFT,Z").debug().hotkey()
@JvmField val MAX_CHAIN_UPDATES = RedenConfigInteger("maxChainUpdates", -1).debug()
@JvmField val DO_ASSERTION_CHECKS = RedenConfigBoolean("doAssertionChecks", false).debug()
@JvmField val UNDO_REPORT_UN_TRACKED_TNT = RedenConfigBoolean("undoReportUnTrackedTnt", false).debug()
@JvmField val OPEN_GITHUB_AUTH_SCREEN = RedenConfigHotkey("openGithubAuthScreen", "R,G").debug().hotkey()
@JvmField val GITHUB_TOKEN = RedenConfigString("githubToken", "").debug()
@JvmField val ALLOW_SOCIAL_FOLLOW = RedenConfigBoolean("allowSocialFollow", true).debug()
@JvmField val DEBUG_RVC_REQUEST_SYNC_DATA = RedenConfigHotkey("debugRvcRequestSyncData", "").debug().hotkey()
@JvmField val SPONSOR_SCREEN_KEY = RedenConfigHotkey("sponsorScreenKey", "").debug().hotkey()
@JvmField val CREDIT_SCREEN_KEY = RedenConfigHotkey("creditScreenKey", "").debug().hotkey()
@JvmField val DEBUG_VIEW_ALL_CONFIGS = RedenConfigHotkey("debugViewAllConfigs", "").debug().hotkey()
@JvmField val DEVELOPER_MODE = RedenConfigBoolean("developerMode", false).debug()
@JvmField val LOCAL_API_BASEURL = RedenConfigString("localApiAddr", "http://localhost:10005/api").debug()
// Hidden
@JvmField val iEVER_USED_UNDO = RedenConfigBoolean("iEverUsedUndo", false).hidden()
@JvmField val iNOTIFICATIONS_ENABLED = RedenConfigBoolean("iNotificationsEnabled", true).hidden()
@JvmField val iSHOW_TIME_OUT_NOTIFICATION = RedenConfigBoolean("iShowTimeOutNotification", true).hidden()
@JvmField val iPRIVACY_SETTING_SHOWN = RedenConfigBoolean("iPrivacySettingShown", false).hidden()
@JvmField val data_BASIC = RedenConfigBoolean("dataBasic", true).hidden()
@JvmField val data_USAGE = RedenConfigBoolean("dataUsage", true).hidden()
@JvmField val data_IDENTIFICATION = RedenConfigBoolean("dataIdentification", false).hidden()

fun getAllOptions() = GENERIC_TAB + RVC_TAB + MICRO_TICK_TAB + SUPER_RIGHT_TAB + DEBUG_TAB + HIDDEN_TAB
