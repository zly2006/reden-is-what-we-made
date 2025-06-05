package com.github.zly2006.reden.utils.multiver

import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.entity.player.Player
import java.net.URI

object Text {
    fun literal(text: String) = Component.literal(text)
    fun of(text: String?) = Component.nullToEmpty(text)
    fun empty() = Component.empty()
    fun translatable(key: String, vararg args: Any): Component {
        return Component.translatable(key, *args)
    }
}

fun MutableComponent.clickOpenUrl(url: String) = apply {
    withStyle { style ->
        style.withClickEvent(ClickEvent.OpenUrl(URI(url)))
    }
}
fun MutableComponent.hoverShowText(text: String) = apply {
    withStyle { style ->
        style.withHoverEvent(HoverEvent.ShowText(Component.literal(text)))
    }
}

fun Player.sendSystemMessage(text: Component) {
    displayClientMessage(text, false)
}
