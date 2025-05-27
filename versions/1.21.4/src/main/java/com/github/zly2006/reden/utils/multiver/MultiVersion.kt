package com.github.zly2006.reden.utils.multiver

import net.minecraft.network.chat.Component

object Text {
    fun literal(text: String) = Component.literal(text)
    fun of(text: String?) = Component.nullToEmpty(text)
    fun translatable(key: String, vararg args: Any): Component {
        return Component.translatable(key, *args)
    }
}
