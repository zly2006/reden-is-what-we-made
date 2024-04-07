package com.github.zly2006.reden.gui.componments

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.utils.plus
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext

class UpdatableTextBox(
    horizontalSizing: Sizing,
    verticalSize: Int,
    defaultValue: String = "",
    val updateCallback: (String, String) -> Boolean
): FlowLayout(horizontalSizing, Sizing.fixed(verticalSize), Algorithm.HORIZONTAL) {
    private var oldValue = defaultValue
    private val input = object : TextBoxComponent(Sizing.fill()) {
        init {
            text(defaultValue)
        }

        override fun renderWidget(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
            super.renderWidget(context, mouseX, mouseY, delta)
        }

        override fun inflate(space: Size) {
            super.inflate(Size.of(space.width - verticalSize, space.height))
        }
    }
    private val updateButton: TextureButtonComponent by lazy {
        TextureButtonComponent(Reden.identifier("check-white.png"), {
            updateContent(oldValue, input.text)
            oldValue = input.text
        }, verticalSize, verticalSize)
    }

    init {
        updateButton.active = false
        input.setDrawsBackground(false)
        input.margins(fuckMojangMargins)
        input.onChanged().subscribe {
            updateButton.active = it != oldValue
        }
        child(input)
        child(updateButton)

        verticalAlignment(VerticalAlignment.CENTER)
        surface(Surface.flat(0x80000000.toInt()) + Surface.outline(0x80FFFFFF.toInt()))
    }

    private fun updateContent(oldValue: String, newValue: String) {
        if (!updateCallback(oldValue, newValue)) {
            input.text(oldValue)
        } else {
            updateButton.active = false
        }
    }
}

/**
 * To keep our text in the center of the textbox.
 * Don't ask me why top is greater than bottom for 1px, ask Mojang.
 */
private val fuckMojangMargins = Insets.of(2, 1, 2, 2)
