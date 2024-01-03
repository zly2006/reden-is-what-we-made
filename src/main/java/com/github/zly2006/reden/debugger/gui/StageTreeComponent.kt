package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.utils.red
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.*
import net.minecraft.text.Text

/**
 * The component that displays the tick stage tree.
 */
class StageTreeComponent(
    val debugger: DebuggerComponent,
    horizontalSizing: Sizing, verticalSizing: Sizing
): ScrollContainer<FlowLayout>(
    ScrollDirection.VERTICAL,
    horizontalSizing,
    verticalSizing,
    Containers.verticalFlow(Sizing.content(), Sizing.content())
) {
    val TickStage.shouldShow: Boolean get() = when (displayLevel) {
        TickStage.DisplayLevel.ALWAYS_HIDE -> false
        TickStage.DisplayLevel.HIDE -> children.any { it.shouldShow }
        TickStage.DisplayLevel.ALWAYS_FOLD -> true
        TickStage.DisplayLevel.FULL -> true
    }

    /**
     * A node in the tree.
     */
    inner class Node(
        private val indent: Int,
        val stage: TickStage,
    ): FlowLayout(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL) {
        /**
         * The x coordinate of the line that highlights this node.
         *
         * This highlight can help users to easily know what stages are at the same level (have same parent).
         */
        var highlightX = -1
        var expanded: Boolean = false
            set(value) {
                if (stage.displayLevel == TickStage.DisplayLevel.ALWAYS_FOLD) return // skip

                button.message = Text.literal(if (value) "-" else "+")
                button.tooltip(Text.literal(if (value) "Fold" else "Expand"))
                field = value
            }

        /**
         * The button to expand or fold the node.
         */
        private val button by lazy {
            Components.button(Text.literal(if (expanded) "-" else "+")) {
                expanded = !expanded
                refresh()
            }.apply {
                sizing(Sizing.fixed(13), Sizing.fixed(12))
                mouseEnter().subscribe {
                    fun recursive(node: Node) {
                        node.highlightX = indent * 6
                        if (node.expanded) {
                            node.childrenNodes.forEach { recursive(it) }
                        }
                    }
                    recursive(this@Node)
                    highlightX = -1
                }
                mouseLeave().subscribe {
                    fun recursive(node: Node) {
                        if (node.highlightX == indent * 6) {
                            node.highlightX = -1
                        }
                        if (node.expanded) {
                            node.childrenNodes.forEach { recursive(it) }
                        }
                    }
                    recursive(this@Node)
                }
            }
        }

        /**
         * If this stage has [TickStage.hasScheduledTicks], [TickStage.hasBlockEvents] or [TickStage.changedBlocks] is not empty,
         *
         * Tell the user that this stage has made some changes to the game.
         */
        private val notification = Components.label(Text.literal(" ")).apply {
            surface(Surface.PANEL)
            sizing(Sizing.fixed(10))
        }

        val childrenNodes = stage.children
            .filter { it.shouldShow }
            .map { Node(indent + 1, it) }

        init {
            val buttonShown = if (childrenNodes.isNotEmpty()) {
                if (stage.displayLevel == TickStage.DisplayLevel.ALWAYS_FOLD) {
                    button.active(false)
                }
                child(button)
                true
            } else false
            child(Components.label(stage.displayName).apply {
                tooltip(stage.description)
                verticalSizing(Sizing.fixed(12))
                padding(Insets.left(6 * indent + if (buttonShown) 0 else 13))
                verticalTextAlignment(VerticalAlignment.CENTER)
                mouseDown().subscribe { _, _, b ->
                    if (b == 0) {
                        debugger.focused = stage
                        true
                    } else false
                }
            })
            if (stage.hasBlockEvents || stage.hasScheduledTicks || stage.changedBlocks.isNotEmpty()) {
                child(notification)
                val tooltipText = mutableListOf<Text>()
                if (stage.hasBlockEvents) {
                    tooltipText.add(Text.literal("Block events").red())
                }
                if (stage.hasScheduledTicks) {
                    tooltipText.add(Text.literal("Scheduled ticks").red())
                }
                if (stage.changedBlocks.isNotEmpty()) {
                    tooltipText.add(Text.literal("Changed blocks").red())
                }
                notification.tooltip(tooltipText.joinToText(Text.literal("\n")))
            }
        }

        override fun draw(context: OwoUIDrawContext?, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            if (highlightX != -1) {
                context?.drawLine(
                    highlightX, y,
                    highlightX, y + height,
                    0.5, Color.ofArgb(0x7fffffff)
                )
            }
        }

        fun appendChildren() {
            if (!stage.shouldShow) return // skip

            child.child(this)
            if (expanded) {
                childrenNodes.forEach {
                    it.highlightX = -1
                    it.appendChildren()
                }
            }
            if (debugger.focused == stage) {
                surface(Surface.flat(0x80_00_00_FF.toInt()))
            } else {
                surface(Surface.VANILLA_TRANSLUCENT)
            }
        }
    }
    val root by lazy { Node(0, debugger.stageTree.activeStages.first()) }
    init {
        if (debugger.stageTree.activeStages.isEmpty()) {
            child.child(Components.label(Text.literal("Fatal: No tick stages present.").red()))
        } else {
            // expand all nodes in stageTree.activeStages
            var node = root
            root.expanded = true
            for (stage in debugger.stageTree.activeStages.drop(1)) {
                node = node.childrenNodes.first { it.stage == stage }
                node.expanded = true
            }
            root.appendChildren()
        }
    }

    fun refresh() {
        val offset = scrollOffset
        child.clearChildren()
        if (debugger.stageTree.activeStages.isEmpty()) {
            child.child(Components.label(Text.literal("Fatal: No tick stages present.").red()))
        } else {
            root.appendChildren()
        }
        scrollOffset = offset
    }
}

/**
 * Something just like [joinToString] but for [Text].
 */
private fun Iterable<Text>.joinToText(literal: Text): Text {
    val builder = Text.empty()
    var first = true
    for (text in this) {
        if (first) first = false
        else builder.append(literal)
        builder.append(text)
    }
    return builder
}
