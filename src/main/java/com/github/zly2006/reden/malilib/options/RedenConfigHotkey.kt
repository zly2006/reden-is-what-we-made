package com.github.zly2006.reden.malilib.options

import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.hotkeys.KeybindSettings

class RedenConfigHotkey(name: String, defaultStorage: String, keybindSettings: KeybindSettings = KeybindSettings.DEFAULT) : IRedenConfigBase,
    ConfigHotkey(name, defaultStorage, keybindSettings, REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX)
