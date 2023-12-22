package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.network.GlobalStatus.Companion.FROZEN
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

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
    val treeComponent by lazy {
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
        child(treeComponent)
        // 30% height for infobox
    }

    override fun onKeyPress(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val index = stageTree.activeStages.indexOf(focused)
        if (keyCode == GLFW.GLFW_KEY_UP) {
            if (index > 0) {
                focused = stageTree.activeStages[index - 1]
            }
            return true
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            if (index < stageTree.activeStages.size - 1) {
                focused = stageTree.activeStages[index + 1]
            }
            return true
        }
        return super.onKeyPress(keyCode, scanCode, modifiers)
    }

    @Deprecated("", level = DeprecationLevel.WARNING)
    class StageNodeComponent(
        val stage: TickStage,
        val rootComponent: DebuggerComponent,
        verticalSizing: Sizing = Sizing.fixed(9),
    ) : FlowLayout(Sizing.content(), verticalSizing, Algorithm.HORIZONTAL) {
        init {
            child(Components.label(stage.displayName).apply {
                this.tooltip(stage.description)
                this.mouseDown().subscribe { _, _, b ->
                    if (b == 0) {
                        rootComponent.focused = stage
                        true
                    } else false
                }
            })
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            val color = if (stage == rootComponent.focused) {
                0x80_00_00_FF.toInt()
            } else if (hovered) {
                0x80_00_00_80.toInt()
            } else {
                0x80_00_00_00.toInt()
            }
            context.fill(
                x,
                y,
                x + determineHorizontalContentSize(Sizing.content()),
                y + 9,
                color
            )
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }

    @Deprecated("", level = DeprecationLevel.HIDDEN)
    class StageTreeLayout(
        val root: DebuggerComponent
    ): GridLayout(
        Sizing.content(),
        Sizing.content(),
        root.stageTree.activeStages.size + 1,
        3
    ) {
        init {
            child(Components.label(Text.literal("Stage Tree")), 0, 1)
            root.stageTree.activeStages.mapIndexed { index, stage ->
                fun fillChildren(offset: Int) {
                    val subList = root.stageTree.activeStages.subList(0, index + 1).toMutableList()
                    var lastOrNull: TickStage?
                    lastOrNull = subList.removeLast().let {
                        subList.last().children.getOrNull(subList.last().children.indexOf(it) + offset)
                    }
                    while (lastOrNull != null) {
                        subList.add(lastOrNull)
                        lastOrNull = lastOrNull.children.firstOrNull()
                    }
                    root.stageTree = TickStageTree(subList)
                    root.focused = null
                    root.treeComponent.refresh()
                }
                child(Components.button(Text.literal("<")) {
                    fillChildren(-1)
                }.verticalSizing(Sizing.fixed(9)), index + 1, 0)

                child(StageNodeComponent(stage, root), index + 1, 1)

                child(Components.button(Text.literal(">")) {
                    fillChildren(1)
                }.verticalSizing(Sizing.fixed(9)), index + 1, 2)
            }
        }
    }

    fun asHud() = apply {
        positioning(Positioning.absolute(0, 0))
    }
}
