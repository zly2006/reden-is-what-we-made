package com.github.zly2006.reden.malilib.options

import fi.dy.masa.malilib.config.options.ConfigBoolean

class RedenConfigBoolean(name: String, defaultValue: Boolean) : IRedenConfigBase,
    ConfigBoolean(name, defaultValue, REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX)
