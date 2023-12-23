package com.github.zly2006.reden.gui.componments

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.text.Text

abstract class UpdatableTextBox(horizontalSizing: Sizing, defaultValue: String = "") : TextBoxComponent(horizontalSizing) {
    var oldValue = defaultValue
    val updateButton by lazy {
        Components.button(Text.literal("Update")) {
            updateContent(oldValue, text)
        }.active(false)!!
    }

    init {
        text = defaultValue
        changedEvents.source().subscribe {
            if (it == oldValue) {
                updateButton.active(false)
            } else {
                updateButton.active(true)
            }
        }
    }

    abstract fun updateContent(oldValue: String, newValue: String)
}
