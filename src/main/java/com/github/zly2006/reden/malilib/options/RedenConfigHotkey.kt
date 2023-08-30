package com.github.zly2006.reden.malilib.options

import fi.dy.masa.malilib.config.options.ConfigHotkey

class RedenConfigHotkey(name: String, defaultStorage: String) : IRedenConfigBase,
    ConfigHotkey(name, defaultStorage, REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX)
