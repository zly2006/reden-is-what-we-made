package com.github.zly2006.reden.malilib.options

import com.github.zly2006.reden.Reden
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.util.StringUtils

const val REDEN_NAMESPACE_PREFIX: String = Reden.MOD_ID + ".config."
const val COMMENT_SUFFIX: String = ".comment"

interface IRedenConfigBase : IConfigBase {
    override fun getConfigGuiDisplayName(): String {
        return StringUtils.translate(REDEN_NAMESPACE_PREFIX + name)
    }
}
