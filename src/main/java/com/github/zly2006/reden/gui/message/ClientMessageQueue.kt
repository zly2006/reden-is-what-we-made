package com.github.zly2006.reden.gui.message

import net.minecraft.text.Text
import net.minecraft.util.Identifier

object ClientMessageQueue {
    class Button(
        val message: Text,
        val action: () -> Unit
    )
    data class Message(
        val icon: Identifier,
        val title: Text,
        val body: Text,
        val buttons: List<Button>
    )
    val messages: MutableList<Message> = mutableListOf()
    fun add(icon: Identifier, title: Text, body: Text, buttons: List<Button>): Int {
        // todo
        return -1
    }
    fun remove(id: Int) {
        messages.removeAt(id)
        // todo: refresh screen
    }
    fun openScreen() {
        TODO()
    }
}
