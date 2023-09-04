package com.github.zly2006.reden.malilib.options

import com.google.common.collect.ImmutableList
import fi.dy.masa.malilib.config.options.ConfigStringList

class RedenConfigStringList(name: String, defaultValue: ImmutableList<String>) : IRedenConfigBase,
    ConfigStringList(name, defaultValue, REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX)
