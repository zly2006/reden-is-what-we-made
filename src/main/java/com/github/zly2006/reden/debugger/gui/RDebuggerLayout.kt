@file:Suppress("FunctionName")

package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.Reden
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.hud.Hud
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun RDebuggerLayout(): GridLayout {
    val grid = Containers.grid(
        Sizing.fill(100),
        Sizing.fill(100),
        1,
        1
    )
    grid.child(
        Components.label(
            Text.literal("RDebugger").formatted(Formatting.RED)
        ), 0, 0
    )
    grid.positioning(Positioning.absolute(0, 0))
    return grid
}

fun register() {
    Hud.add(
        Reden.identifier("r-debugger"),
        ::RDebuggerLayout
    )
}
