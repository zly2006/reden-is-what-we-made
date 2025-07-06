package com.github.zly2006.reden.utils.multiver

import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent

object Text {
    fun literal(text: String) = Component.literal(text)
    fun of(text: String?) = Component.nullToEmpty(text)
    fun empty() = Component.empty()
    fun translatable(key: String, vararg args: Any): Component {
        return Component.translatable(key, *args)
    }
}

fun sendSystemMessage() {
    // dummy function to avoid import issues
}

fun MutableComponent.clickOpenUrl(url: String) = apply {
    withStyle { style ->
        style.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url))
    }
}
fun MutableComponent.hoverShowText(text: String) = apply {
    withStyle { style ->
        style.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(text)))
    }
}
