package com.github.zly2006.reden.malilib.options

import fi.dy.masa.malilib.config.options.ConfigInteger

class RedenConfigInteger : IRedenConfigBase, ConfigInteger {
    constructor(name: String, defaultValue: Int) : super(
        name,
        defaultValue,
        REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX
    )

    constructor(name: String, defaultValue: Int, minValue: Int, maxValue: Int) : super(
        name,
        defaultValue,
        minValue,
        maxValue,
        REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX
    )
}
