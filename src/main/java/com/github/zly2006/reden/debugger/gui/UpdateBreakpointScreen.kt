package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.OwoUIAdapter

class UpdateBreakpointScreen(
    val breakpoint: BreakPoint
): BaseOwoScreen<ScrollContainer<FlowLayout>>() {
    override fun createAdapter() = OwoUIAdapter.create(this) { horizontal, vertical ->
        Containers.verticalScroll(horizontal, vertical, Containers.verticalFlow(horizontal, vertical))
    }!!

    override fun build(p0: ScrollContainer<FlowLayout>) {
        val root = p0.child()
    }

    class BreakpointBehaviorComponent {
    }
}
