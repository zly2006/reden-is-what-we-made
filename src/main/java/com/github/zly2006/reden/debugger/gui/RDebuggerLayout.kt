@file:Suppress("FunctionName")

package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.TickStage
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import io.wispforest.owo.ui.hud.Hud

val setDescriptionInfo = mutableListOf<(TickStage) -> Unit>()

fun RDebuggerLayout(): GridLayout {
    val grid = Containers.grid(
        Sizing.fill(100),
        Sizing.fill(100),
        1,
        1
    )
    grid.positioning(Positioning.absolute(0, 0))
    grid.verticalAlignment(VerticalAlignment.TOP)
    return grid
}

fun register() {
    Hud.add(
        Reden.identifier("r-debugger-tools"),
        ::RDebuggerLayout
    )
    Hud.add(
        Reden.identifier("r-debugger-stage"),
        ::RDebuggerLayout
    )
    Hud.add(
        Reden.identifier("r-debugger-stack"),
        ::RDebuggerLayout
    )
}
