@file:Suppress("HasPlatformType")
@file:Environment(EnvType.CLIENT)

package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.malilib.options.*
import com.github.zly2006.reden.render.SolidFaceRenderer.ShapePredicateOptionEntry
import com.github.zly2006.reden.utils.startDebugAppender
import com.github.zly2006.reden.utils.stopDebugAppender
import fi.dy.masa.malilib.config.HudAlignment
import fi.dy.masa.malilib.config.options.ConfigBase
import fi.dy.masa.malilib.config.options.ConfigOptionList
import fi.dy.masa.malilib.hotkeys.IHotkey
import fi.dy.masa.malilib.hotkeys.KeybindSettings
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import com.github.zly2006.reden.malilib.options.RedenConfigBooleanHotkeyed as RCBooleanHotkey

// @formatter:off
@JvmField val HOTKEYS = mutableListOf<IHotkey>()
@JvmField val GENERIC_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val RVC_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val MICRO_TICK_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val SUPER_RIGHT_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val DEBUG_TAB = mutableListOf<ConfigBase<*>>()
private fun <T : IHotkey> T.hotkey() = this.apply(HOTKEYS::add)
private fun <T : ConfigBase<*>> T.generic() = apply { (GENERIC_TAB.add(this)) }
private fun <T : ConfigBase<*>> T.rvc() = apply { (RVC_TAB.add(this)) }
private fun <T : ConfigBase<*>> T.mt() = apply { (MICRO_TICK_TAB.add(this)) }
private fun <T : ConfigBase<*>> T.sr() = apply { (SUPER_RIGHT_TAB.add(this)) }
private fun <T : ConfigBase<*>> T.debug() = apply { (DEBUG_TAB.add(this)) }

// Generic
@JvmField val REDEN_CONFIG_KEY = RedenConfigHotkey("redenConfigKey", "R,C").generic().hotkey()
@JvmField val SELECTION_TOOL = RedenConfigString("selectionTool", "minecraft:blaze_rod").generic()
@JvmField val HUD_POSITION = ConfigOptionList("hudPosition", HudAlignment.BOTTOM_LEFT, "").generic()
@JvmField val NO_TIME_OUT = RedenConfigBoolean("noTimeOut").generic()
@JvmField val BLOCK_BORDER_ALPHA = RedenConfigFloat("blockBorderAlpha", 0.1f, 0f, 1f).generic()
@JvmField val UNDO_KEY = RedenConfigHotkey("undoKey", "LEFT_CONTROL,Z").generic().hotkey()
@JvmField val REDO_KEY = RedenConfigHotkey("redoKey", "LEFT_CONTROL,Y").generic().hotkey()
@JvmField val UNDO_SUPPORT_LITEMATICA_OPERATION = RedenConfigBoolean("undoSupportLitematicaOperation", true).generic()
@JvmField val UNDO_CHEATING_ONLY = RedenConfigBoolean("undoCheatingOnly", true).generic()
@JvmField val OPEN_NOTIFICATIONS_SCREEN = RedenConfigHotkey("openNotificationsScreen", "R,T").generic().hotkey()
@JvmField val MAX_RENDER_DISTANCE = RedenConfigInteger("maxRenderDistance", 48).generic()
@JvmField val SOLID_FACE_RENDERER = RedenConfigBoolean("solidFaceRenderer").generic()
@JvmField val ENABLE_CLIENT_GLOW = RCBooleanHotkey("enableClientGlow", true, "LEFT_CONTROL,G").hotkey().generic()
@JvmField val SOLID_FACE_SHAPE_PREDICATE = RedenConfigOptionList("solidFaceShapePredicate", ShapePredicateOptionEntry.FULL).generic()
@JvmField val FANCY_BLOCK_OUTLINE = RedenConfigBoolean("fancyBlockOutline", true).generic()
@JvmField val EASTER_EGG_RATE = RedenConfigInteger("easterEggRate", 3, 0, 100).generic()
@JvmField val SCROLL_AMOUNT = RedenConfigFloat("scrollAmount", 1.5f, 0f, 5f).generic()
// Micro Tick
@JvmField
val BREAKPOINT_RENDERER = RCBooleanHotkey("breakpointRenderer", false, "B", KeybindSettings.INGAME_BOTH).hotkey().mt()
@JvmField val EDIT_BREAKPOINTS = RedenConfigHotkey("editBreakpoints", "B,BUTTON_1").mt().hotkey()
@JvmField val ADD_BREAKPOINT = RedenConfigHotkey("addBreakpoint", "B,BUTTON_2").mt().hotkey()
@JvmField val VIEW_ALL_BREAKPOINTS = RedenConfigHotkey("viewAllBreakpoints", "RIGHT_SHIFT,SPACE").mt().hotkey()
@JvmField val PAUSE_KEY = RedenConfigHotkey("pauseKey", "LEFT_CONTROL,LEFT_SHIFT,P").mt().hotkey()
@JvmField val CONTINUE_KEY = RedenConfigHotkey("continueKey", "").mt().hotkey()
@JvmField val STEP_INTO_KEY = RedenConfigHotkey("stepIntoKey", "").mt().hotkey()
@JvmField val STEP_OVER_KEY = RedenConfigHotkey("stepOverKey", "").mt().hotkey()
@JvmField val TOGGLE_FORCE_ENTITY_POS_SYNC = RCBooleanHotkey("toggleForceEntityPosSync", false, "LEFT_CONTROL,LEFT_SHIFT,V").hotkey().mt()
// RVC
@JvmField val OPEN_RVC_SCREEN = RedenConfigHotkey("openRvcScreen", "R", KeybindSettings.RELEASE).rvc().hotkey()
@JvmField val OPEN_SELECTION_LIST = RedenConfigHotkey("openSelectionList", "R,L").rvc().hotkey()
@JvmField val OPEN_EXPORT_SCREEN = RedenConfigHotkey("openExportScreen", "R,E").rvc().hotkey()
@JvmField val OPEN_IMPORT_SCREEN = RedenConfigHotkey("openImportScreen", "R,I").rvc().hotkey()
@JvmField val RVC_SAVE_KEY = RedenConfigHotkey("redenSaveKey", "LEFT_CONTROL,S").rvc().hotkey()
@JvmField val RVC_RECORD_MULTIPLAYER = RedenConfigBoolean("rvcRecordMultiplayer", true).rvc()
@JvmField val RVC_FORCE_LOCALLY = RedenConfigBoolean("rvcForceLocally").rvc()
@JvmField val RVC_CONFIRM_KEY = RedenConfigHotkey("rvcConfirmKey", "RIGHT_SHIFT,ENTER").rvc().hotkey()
@JvmField val RVC_CANCEL_KEY = RedenConfigHotkey("rvcCancelKey", "BACKSPACE").rvc().hotkey()
// Super Right
@JvmField val CHAT_RIGHT_CLICK_MENU = RedenConfigBoolean("chatRightClickMenu", true).sr()
@JvmField val STRUCTURE_BLOCK_LOAD = RedenConfigHotkey("structureBlockLoad", "").sr().hotkey()
@JvmField val STRUCTURE_BLOCK_SAVE = RedenConfigHotkey("structureBlockSave", "").sr().hotkey()
@JvmField val WORMHOLE_SELECT = RedenConfigHotkey("wormholeSelect", "LEFT_ALT", KeybindSettings.INGAME_BOTH).sr().hotkey()
@JvmField val RUN_COMMAND = RedenConfigCommandHotkeyList("runCommand").sr()
// Debug
@JvmField val DEBUG_LOGGER = RedenConfigBoolean("debugLogger") {
    if (booleanValue) startDebugAppender()
    else stopDebugAppender()
}.debug()
@JvmField val DEBUG_PACKET_LOGGER = RedenConfigBoolean("debugPacketLogger").debug()
@JvmField val DEBUG_TAG_BLOCK_POS = RedenConfigHotkey("debugTagBlockPos", "LEFT_CONTROL,LEFT_SHIFT,T").debug().hotkey()
@JvmField val DEBUG_LOGGER_IGNORE_UNDO_ID_0 = RedenConfigBoolean("debugLoggerIgnoreUndoId0").debug()
@JvmField val DEBUG_PREVIEW_UNDO = RedenConfigHotkey("debugPreviewUndo", "LEFT_CONTROL,LEFT_SHIFT,Z").debug().hotkey()
@JvmField val MAX_CHAIN_UPDATES = RedenConfigInteger("maxChainUpdates", -1).debug()
@JvmField val DO_ASSERTION_CHECKS = RedenConfigBoolean("doAssertionChecks").debug()
@JvmField val UNDO_REPORT_UN_TRACKED_TNT = RedenConfigBoolean("undoReportUnTrackedTnt").debug()
@JvmField val OPEN_GITHUB_AUTH_SCREEN = RedenConfigHotkey("openGithubAuthScreen", "R,G").debug().hotkey()
@JvmField val GITHUB_TOKEN = RedenConfigString("githubToken", "").debug()
@JvmField val SPONSOR_SCREEN_KEY = RedenConfigHotkey("sponsorScreenKey", "").debug().hotkey()
@JvmField val CREDIT_SCREEN_KEY = RedenConfigHotkey("creditScreenKey", "").debug().hotkey()
@JvmField val DEBUG_VIEW_ALL_CONFIGS = RedenConfigHotkey("debugViewAllConfigs", "").debug().hotkey()
@JvmField val DEVELOPER_MODE = RedenConfigBoolean("developerMode").debug()
@JvmField val LOCAL_API_BASEURL = RedenConfigString("localApiAddr", "http://localhost:10005/api").debug()
@JvmField val ENTITY_OUTLINE_RENDER_RAW = RedenConfigBoolean("entityOutlinrRenderRaw").debug()
@JvmField val DEBUG_DISPLAY_RVC_WORLD_INFO = RedenConfigHotkey("debugDisplayRvcWorldInfo").debug().hotkey()
@JvmField val DEBUG_NEW_NOTIFICATION = RedenConfigHotkey("debugNewNotification", "").debug().hotkey()
@JvmField val DEBUG_LITEMATICA_SCHEMATIC_RERENDER = RedenConfigHotkey("debugLitematicaSchematicRerender").debug().hotkey()

// Hidden
object HiddenOption {
    private fun <T : ConfigBase<*>> T.hidden() = apply { (HIDDEN_TAB.add(this)) }
    @JvmField val HIDDEN_TAB = mutableListOf<ConfigBase<*>>()
    @JvmField val iCHECK_UPDATES = RedenConfigBoolean("iNotificationsEnabled", true).hidden()
    @JvmField val iPRIVACY_SETTING_SHOWN = RedenConfigBoolean("iPrivacySettingShown").hidden()
    @JvmField val data_BASIC = RedenConfigBoolean("dataBasic", true).hidden()
    @JvmField val data_USAGE = RedenConfigBoolean("dataUsage", true).hidden()
    @JvmField val data_IDENTIFICATION = RedenConfigBoolean("dataIdentification").hidden()
}
// @formatter:on

fun getAllOptions() = GENERIC_TAB + RVC_TAB + MICRO_TICK_TAB + SUPER_RIGHT_TAB + DEBUG_TAB + HiddenOption.HIDDEN_TAB
