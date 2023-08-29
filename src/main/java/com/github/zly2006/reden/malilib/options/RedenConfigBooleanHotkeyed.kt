package com.github.zly2006.reden.malilib.options

import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed

class RedenConfigBooleanHotkeyed(name: String, defaultValue: Boolean, defaultHotkey: String) : IRedenConfigBase,
    ConfigBooleanHotkeyed(name, defaultValue, defaultHotkey, REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX)
