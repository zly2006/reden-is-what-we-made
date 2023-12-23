package com.github.zly2006.reden.gui.componments

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.gui.fuckMojangMargins
import com.github.zly2006.reden.utils.plus
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.client.gui.DrawContext

class UpdatableTextBox(
    horizontalSizing: Sizing,
    verticalSize: Int,
    defaultValue: String = "",
    val updateCallback: (String, String) -> Boolean
): FlowLayout(horizontalSizing, Sizing.fixed(verticalSize), Algorithm.HORIZONTAL) {
    var oldValue = defaultValue
    val input = object: TextBoxComponent(Sizing.fill()) {
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
    val updateButton: TextureButtonComponent by lazy {
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

    fun updateContent(oldValue: String, newValue: String) {
        if (!updateCallback(oldValue, newValue)) {
            input.text(oldValue)
        } else {
            updateButton.active = false
        }
    }
}
