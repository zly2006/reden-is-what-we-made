package com.github.zly2006.reden.utils

import com.github.zly2006.reden.Reden
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.text.Text


private const val MESSAGE_PREFIX: String = Reden.MOD_ID + ".message."

fun translateMessage(category: String, key: String, vararg args: Any): Text {
    val msg = StringUtils.translate("$MESSAGE_PREFIX$category.$key", args)
    return Text.translatable("$MESSAGE_PREFIX$category.base", msg)
}

