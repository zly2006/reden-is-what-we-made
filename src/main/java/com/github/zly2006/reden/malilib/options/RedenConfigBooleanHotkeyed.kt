package com.github.zly2006.reden.malilib.options

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed
import fi.dy.masa.malilib.hotkeys.KeybindSettings

class RedenConfigBooleanHotkeyed(
    name: String,
    defaultValue: Boolean,
    defaultHotkey: String,
    settings: KeybindSettings = KeybindSettings.DEFAULT
) : ConfigBooleanHotkeyed(
    name, defaultValue, defaultHotkey, settings,
    REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX, REDEN_NAMESPACE_PREFIX + name
), IRedenConfigBase
