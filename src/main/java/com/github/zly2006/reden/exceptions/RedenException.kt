package com.github.zly2006.reden.exceptions

import net.minecraft.text.Text

/**
 * Reden mod base exception
 */
class RedenException : Exception {
    val displayMessage: Text

    constructor(message: String) : super(message) {
        this.displayMessage = Text.of(message)
    }

    constructor(message: Text) : super(message.string) {
        this.displayMessage = message
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        this.displayMessage = Text.of(message)
    }

    constructor(message: Text, cause: Throwable) : super(message.string, cause) {
        this.displayMessage = message
    }

    override fun toString(): String {
        return displayMessage.string
    }
}
