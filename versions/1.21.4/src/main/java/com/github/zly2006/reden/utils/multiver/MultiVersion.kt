package com.github.zly2006.reden.utils.multiver

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player

object Text {
    fun literal(text: String) = Component.literal(text)
    fun of(text: String?) = Component.nullToEmpty(text)
    fun translatable(key: String, vararg args: Any): Component {
        return Component.translatable(key, *args)
    }
}

fun Player.sendSystemMessage(text: Component) {
    displayClientMessage(text, false)
}
