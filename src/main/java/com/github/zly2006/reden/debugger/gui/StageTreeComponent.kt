package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.TickStage
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
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
        var expanded: Boolean,
    ): FlowLayout(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL) {
        override fun mount(parent: ParentComponent?, x: Int, y: Int) {
            // Lazy init our components
            child(Components.label(stage.displayName).apply {
                tooltip(stage.description)
                verticalSizing(Sizing.fixed(12))
                verticalTextAlignment(VerticalAlignment.CENTER)
                surface(Surface.VANILLA_TRANSLUCENT)
            })
            if (stage.children.isNotEmpty()) {
                child(Components.button(Text.literal(if (expanded) "[-]" else "[+]")) {
                    expanded = !expanded
                    it.tooltip(Text.literal(if (expanded) "Fold" else "Expand"))
                    it.message = Text.literal(if (expanded) "[-]" else "[+]")
                    refresh()
                }.apply {
                    verticalSizing(Sizing.fixed(12))
                })
            }

            super.mount(parent, x, y)
        }

        val childrenNodes = lazy {
            stage.children.map { Node(indent + 1, it, false) }
        }
        fun appendChildren() {
            child.child(this)
            if (expanded) {
                childrenNodes.value.forEach { it.appendChildren() }
            }
            padding(Insets.left(6 * indent))
        }
    }
    val root = Node(0, debugger.stageTree.activeStages.first(), true)
    init {
        // expand all nodes in stageTree.activeStages
        var node = root
        for (stage in debugger.stageTree.activeStages.drop(1)) {
            node = node.childrenNodes.value.first { it.stage == stage }
            node.expanded = true
        }
        root.appendChildren()
    }

    fun refresh() {
        child.clearChildren()
        root.appendChildren()
    }
}
