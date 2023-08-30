package com.github.zly2006.reden.malilib.options

import fi.dy.masa.malilib.config.options.ConfigString

class RedenConfigString(name: String, defaultValue: String) : IRedenConfigBase,
    ConfigString(name, defaultValue, REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX)
