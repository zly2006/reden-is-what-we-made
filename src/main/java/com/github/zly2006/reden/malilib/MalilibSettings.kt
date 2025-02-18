@file:Suppress("HasPlatformType")
@file:Environment(EnvType.CLIENT)

package com.github.zly2006.reden.malilib

import com.github.zly2006.reden.malilib.options.*
import com.github.zly2006.reden.render.SolidFaceRenderer.ShapePredicateOptionEntry
import com.github.zly2006.reden.utils.startDebugAppender
import com.github.zly2006.reden.utils.stopDebugAppender
import fi.dy.masa.malilib.config.options.ConfigBase
import fi.dy.masa.malilib.hotkeys.IHotkey
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import com.github.zly2006.reden.malilib.options.RedenConfigBooleanHotkeyed as RCBooleanHotkey

// @formatter:off
@JvmField val HOTKEYS = mutableListOf<IHotkey>()
@JvmField val GENERIC_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val SUPER_RIGHT_TAB = mutableListOf<ConfigBase<*>>()
@JvmField val DEBUG_TAB = mutableListOf<ConfigBase<*>>()
private fun <T : IHotkey> T.hotkey() = this.apply(HOTKEYS::add)
private fun <T : ConfigBase<*>> T.generic() = apply { (GENERIC_TAB.add(this)) }
private fun <T : ConfigBase<*>> T.sr() = apply { (SUPER_RIGHT_TAB.add(this)) }
private fun <T : ConfigBase<*>> T.debug() = apply { (DEBUG_TAB.add(this)) }

// Generic
@JvmField val REDEN_CONFIG_KEY = RedenConfigHotkey("redenConfigKey", "R,C").generic().hotkey()
@JvmField val UNDO_KEY = RedenConfigHotkey("undoKey", "LEFT_CONTROL,Z").generic().hotkey()
@JvmField val REDO_KEY = RedenConfigHotkey("redoKey", "LEFT_CONTROL,Y").generic().hotkey()
@JvmField val NO_TIME_OUT = RedenConfigBoolean("noTimeOut").generic()
@JvmField val BLOCK_BORDER_ALPHA = RedenConfigFloat("blockBorderAlpha", 0.1f, 0f, 1f).generic()
@JvmField val UNDO_SUPPORT_LITEMATICA_OPERATION = RedenConfigBoolean("undoSupportLitematicaOperation", true).generic()
@JvmField val UNDO_CHEATING_ONLY = RedenConfigBoolean("undoCheatingOnly", true).generic()
@JvmField val OPEN_NOTIFICATIONS_SCREEN = RedenConfigHotkey("openNotificationsScreen", "R,T").generic().hotkey()
@JvmField val MAX_RENDER_DISTANCE = RedenConfigInteger("maxRenderDistance", 48).generic()
@JvmField val SOLID_FACE_RENDERER = RedenConfigBoolean("solidFaceRenderer").generic()
@JvmField val ENABLE_CLIENT_GLOW = RCBooleanHotkey("enableClientGlow", true, "LEFT_CONTROL,G").hotkey().generic()
@JvmField val SOLID_FACE_SHAPE_PREDICATE = RedenConfigOptionList("solidFaceShapePredicate", ShapePredicateOptionEntry.FULL).generic()
@JvmField val MEV_DOWNLOADS = RedenConfigBoolean("mevDownloads", true)
@JvmField val EASTER_EGG_RATE = RedenConfigInteger("easterEggRate", 1, 0, 100).generic()
// Super Right
@JvmField val CHAT_RIGHT_CLICK_MENU = RedenConfigBoolean("chatRightClickMenu", true).sr()
@JvmField val STRUCTURE_BLOCK_LOAD = RedenConfigHotkey("structureBlockLoad", "").sr().hotkey()
@JvmField val STRUCTURE_BLOCK_SAVE = RedenConfigHotkey("structureBlockSave", "").sr().hotkey()
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
@JvmField val SPONSOR_SCREEN_KEY = RedenConfigHotkey("sponsorScreenKey", "").debug().hotkey()
@JvmField val CREDIT_SCREEN_KEY = RedenConfigHotkey("creditScreenKey", "").debug().hotkey()
@JvmField val DEBUG_VIEW_ALL_CONFIGS = RedenConfigHotkey("debugViewAllConfigs", "").debug().hotkey()
@JvmField val DEVELOPER_MODE = RedenConfigBoolean("developerMode").debug()
@JvmField val LOCAL_API_BASEURL = RedenConfigString("localApiAddr", "http://localhost:10005/api").debug()
@JvmField val ENTITY_OUTLINE_RENDER_RAW = RedenConfigBoolean("entityOutlinrRenderRaw").debug()
@JvmField val DEBUG_NEW_NOTIFICATION = RedenConfigHotkey("debugNewNotification", "").debug().hotkey()
@JvmField val DEBUG_MINENV_GUI = RedenConfigHotkey("debugMinenvGui").debug().hotkey()
@JvmField val DEBUG_MINENV_THUMBNAIL_COMPARISON = RedenConfigBoolean("MinenvThumbnailComparison").debug()
@JvmField val DEBUG_MINENV_NO_CACHE = RedenConfigBoolean("MinenvNoCache").debug()

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

fun getAllOptions() = GENERIC_TAB + SUPER_RIGHT_TAB + DEBUG_TAB + HiddenOption.HIDDEN_TAB
