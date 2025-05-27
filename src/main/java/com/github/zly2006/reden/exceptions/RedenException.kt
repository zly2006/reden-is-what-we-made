package com.github.zly2006.reden.exceptions

import com.github.zly2006.reden.utils.multiver.Text
import net.minecraft.network.chat.Component

/**
 * Reden mod base exception
 */
class RedenException : Exception {
    val displayMessage: Component

    constructor(message: String) : super(message) {
        this.displayMessage = Text.of(message)
    }

    constructor(message: Component) : super(message.string) {
        this.displayMessage = message
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        this.displayMessage = Text.of(message)
    }

    constructor(message: Component, cause: Throwable) : super(message.string, cause) {
        this.displayMessage = message
    }

    override fun toString(): String {
        return displayMessage.string
    }
}
