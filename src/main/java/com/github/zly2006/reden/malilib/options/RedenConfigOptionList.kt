package com.github.zly2006.reden.malilib.options

import fi.dy.masa.malilib.config.IConfigOptionListEntry
import fi.dy.masa.malilib.config.options.ConfigOptionList

class RedenConfigOptionList(name : String, defaultValue : IConfigOptionListEntry) : IRedenConfigBase,
        ConfigOptionList(name, defaultValue, REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX)