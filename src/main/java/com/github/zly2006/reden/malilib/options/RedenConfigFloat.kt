package com.github.zly2006.reden.malilib.options

import fi.dy.masa.malilib.config.options.ConfigDouble

class RedenConfigFloat(name: String, defaultValue: Float, minValue: Float, maxValue: Float): IRedenConfigBase, ConfigDouble(
    name, defaultValue.toDouble(), minValue.toDouble(), maxValue.toDouble(), REDEN_NAMESPACE_PREFIX + name + COMMENT_SUFFIX
) {
    val floatValue get() = doubleValue.toFloat()
}
