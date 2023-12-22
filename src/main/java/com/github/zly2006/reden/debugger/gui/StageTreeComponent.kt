package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.TickStage
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.text.Text

class StageTreeComponent(
    val debugger: DebuggerComponent,
    horizontalSizing: Sizing, verticalSizing: Sizing
): ScrollContainer<FlowLayout>(
    ScrollDirection.VERTICAL,
    horizontalSizing,
    verticalSizing,
    Containers.verticalFlow(Sizing.content(), Sizing.content())
) {
    inner class Node(
        val indent: Int,
        val stage: TickStage,
    ): FlowLayout(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL) {
        var expanded: Boolean = false
            set(value) {
                button.message = Text.literal(if (value) "[-]" else "[+]")
                button.tooltip(Text.literal(if (value) "Fold" else "Expand"))
                field = value
            }
        private val button = Components.button(Text.literal(if (expanded) "[-]" else "[+]")) {
            expanded = !expanded
            refresh()
        }.apply {
            verticalSizing(Sizing.fixed(12))
        }
        init {
            child(Components.label(stage.displayName).apply {
                tooltip(stage.description)
                verticalSizing(Sizing.fixed(12))
                verticalTextAlignment(VerticalAlignment.CENTER)
                surface(Surface.VANILLA_TRANSLUCENT)
            })
            if (stage.children.isNotEmpty()) {
                child(button)
            }
        }

        val childrenNodes = lazy {
            stage.children.map { Node(indent + 1, it) }
        }
        fun appendChildren() {
            child.child(this)
            if (expanded) {
                childrenNodes.value.forEach { it.appendChildren() }
            }
            padding(Insets.left(6 * indent))
        }
    }
    val root = Node(0, debugger.stageTree.activeStages.first())
    init {
        // expand all nodes in stageTree.activeStages
        var node = root
        root.expanded = true
        for (stage in debugger.stageTree.activeStages.drop(1)) {
            node = node.childrenNodes.value.first { it.stage == stage }
            node.expanded = true
        }
        root.appendChildren()
    }

    fun refresh() {
        val offset = scrollOffset
        child.clearChildren()
        root.appendChildren()
        scrollOffset = offset
    }
}
