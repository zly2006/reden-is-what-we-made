package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.tree.StageTree
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.text.Text

class DebuggerComponent(
    val stageTree: StageTree
): FlowLayout(Sizing.content(), Sizing.fill(80), Algorithm.VERTICAL) {
    init {
        child(Containers.verticalScroll(Sizing.fill(100), Sizing.fill(60), stageTreeLayout()))
        // 40% height for infobox
    }

    class StageNodeComponent(
        val node: StageTree.TreeNode,
        val lrWidth: Int = 20,
    ): FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.HORIZONTAL) {
        init {
            child(Components.label(node.stage.displayName ?: Text.literal("null")))
        }
    }

    fun stageTreeLayout(): GridLayout {
        val layout = Containers.grid(Sizing.fill(100), Sizing.fill(100), stageTree.depth() + 1, 3)
        layout.child(Components.label(Text.literal("Stage Tree")), 0, 1)
        var node = stageTree.lastReturned
        var depth = stageTree.depth()
        while (node != null) {
            layout.child(StageNodeComponent(node), depth, 0)
            node = node.parent
            depth--
        }
        return layout
    }

    fun asHud() = apply {
        positioning(Positioning.absolute(0, 0))
    }

    fun asScreen() = object: BaseOwoScreen<FlowLayout>() {
        override fun createAdapter(): OwoUIAdapter<FlowLayout> {
            return OwoUIAdapter.create(this, Containers::verticalFlow)
        }

        override fun build(rootComponent: FlowLayout) {
            rootComponent.child(this@DebuggerComponent)
        }
    }
}

private fun StageTree.depth(): Int {
    var depth = 0
    var node = lastReturned
    while (node != null) {
        depth++
        node = node.parent
    }
    return depth
}
