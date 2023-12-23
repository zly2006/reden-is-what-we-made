package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.network.GlobalStatus.Companion.FROZEN
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min

/**
 * Our main debugger UI.
 *
 * It has two forms: as a HUD, or a screen.
 *
 * The screen form will replace the game menu when you press ESC,
 * making it easier for players to have access to our debugger features.
 *
 * We allow player to move (i.e., WASD) while debugging.
 *
 * The HUD form will be shown when game was [FROZEN].
 */
open class DebuggerComponent(
    var stageTree: TickStageTree
): FlowLayout(Sizing.content(), Sizing.fill(70), Algorithm.VERTICAL) {
    private val treeComponent by lazy {
        StageTreeComponent(this, Sizing.fill(), Sizing. fill())
    }
    open var focused: TickStage? = null
        set(value) {
            field?.unfocused(MinecraftClient.getInstance())
            field = value
            value?.focused(MinecraftClient.getInstance())
            treeComponent.refresh()
        }

    init {
        child(Containers.draggable(Sizing.fill(), Sizing.fill(), treeComponent))
        // 30% height for infobox
    }

    final override fun child(child: Component): FlowLayout {
        return super.child(child)
    }

    override fun onKeyPress(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val index = stageTree.activeStages.indexOf(focused)
        val nodes by lazy { treeComponent.child().children().filterIsInstance<StageTreeComponent.Node>() }

        return when (keyCode) {
            GLFW.GLFW_KEY_LEFT -> {
                if (index == -1) {
                    focused = focused?.parent
                } else if (index > 0) {
                    focused = stageTree.activeStages[index - 1]
                }
                true
            }
            GLFW.GLFW_KEY_RIGHT -> {
                nodes.find { it.stage == focused }?.let { it.expanded = it.childrenNodes.isNotEmpty() }
                if (index in 0 until  stageTree.activeStages.lastIndex) {
                    focused = stageTree.activeStages[index + 1]
                }
                true
            }
            GLFW.GLFW_KEY_UP -> {
                focused = nodes[max(nodes.indexOfFirst { it.stage == focused } - 1, 0)].stage
                true
            }
            GLFW.GLFW_KEY_DOWN -> {
                focused = nodes[min(nodes.indexOfFirst { it.stage == focused } + 1, nodes.size - 1)].stage
                true
            }
            GLFW.GLFW_KEY_COMMA -> {
                nodes.find { it.stage == focused }?.let {
                    if (it.childrenNodes.isNotEmpty()) {
                        it.expanded = true
                        treeComponent.refresh()
                    }
                }
                true
            }
            GLFW.GLFW_KEY_PERIOD -> {
                nodes.find { it.stage == focused }?.let {
                    if (it.childrenNodes.isNotEmpty()) {
                        it.expanded = false
                        treeComponent.refresh()
                    }
                    else {
                        focused = focused?.parent
                    }
                }
                true
            }
            else -> super.onKeyPress(keyCode, scanCode, modifiers)
        }
    }

    fun asHud() = apply {
        positioning(Positioning.absolute(0, 0))
    }
}
