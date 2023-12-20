package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.mixin.otherMods.IScrollContainer
import com.github.zly2006.reden.network.Continue
import com.github.zly2006.reden.network.GlobalStatus.Companion.FROZEN
import com.github.zly2006.reden.network.StepInto
import com.github.zly2006.reden.network.StepOver
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ButtonTextures
import net.minecraft.client.gui.screen.GameMenuScreen
import net.minecraft.client.gui.widget.TexturedButtonWidget
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
class DebuggerComponent(
    val stageTree: TickStageTree
): FlowLayout(Sizing.content(), Sizing.fill(70), Algorithm.VERTICAL) {
    var focused: TickStage? = null
        set(value) {
            field?.unfocused(MinecraftClient.getInstance())
            field = value
            value?.focused(MinecraftClient.getInstance())
            refreshStages()
        }

    init {
        refreshStages()
        // 30% height for infobox
    }

    fun refreshStages() {
        val sizing = Sizing.fill()
        val scrolledAmount = (children.getOrNull(0) as IScrollContainer?)?.scrollOffset ?: 0.0
        children.clear()
        child(Containers.verticalScroll(Sizing.fill(100), sizing, StageTreeLayout(this)).apply {
            (this as IScrollContainer).scrollOffset = scrolledAmount
            (this as IScrollContainer).currentScrollPosition = scrolledAmount
        })
        child(Components.block(Blocks.ACACIA_LOG.defaultState).sizing(Sizing.fixed(30)))
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

    class StageNodeComponent(
        val stage: TickStage,
        val rootComponent: DebuggerComponent,
        val lrWidth: Int = 20,
    ) : FlowLayout(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL) {
        init {
            child(Components.label(stage.displayName).apply {
                this.tooltip(stage.description)
                this.mouseDown().subscribe { x, y, b ->
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
                y + determineVerticalContentSize(Sizing.fill(100)),
                color
            )
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }

    class StageTreeLayout(
        val root: DebuggerComponent
    ): GridLayout(
        Sizing.fill(100),
        Sizing.content(),
        root.stageTree.activeStages.size + 1,
        3
    ) {
        init {
            child(Components.label(Text.literal("Stage Tree")), 0, 1)
            root.stageTree.activeStages.mapIndexed { index, stage ->
                child(StageNodeComponent(stage, root), index + 1, 0)
            }
        }
    }

    fun asHud() = apply {
        positioning(Positioning.absolute(0, 0))
    }

    fun asScreen(): BaseOwoScreen<FlowLayout> = DebuggerScreen(this)
}

private class DebuggerScreen(private val component: DebuggerComponent): BaseOwoScreen<FlowLayout>() {
    val actionList by lazy {
        client!!.options.run {
            listOf(forwardKey, leftKey, backKey, rightKey, jumpKey, sneakKey)
        }
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, Containers::verticalFlow)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent.child(component)
        rootComponent.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
            child(Components.button(Text.literal("Continue")) {
                ClientPlayNetworking.send(Continue())
            })
            child(Components.wrapVanillaWidget(TexturedButtonWidget(
                16, 16, ButtonTextures(Reden.identifier("reden-icon.png"), Reden.identifier("reden-icon.png")), {

                }, Text.literal("C")
            )))
            child(Components.texture(
                Reden.identifier("reden-icon.png"),0,0,16,16,
                160, 160
            ).blend(true))
            child(Components.button(Text.literal("Step Into")) {
                ClientPlayNetworking.send(StepInto())
            })
            child(Components.button(Text.literal("Step Over")) {
                ClientPlayNetworking.send(StepOver(false /* This value does not matter */))
            })
        })
        rootComponent.child(Components.button(Text.literal("Open Game Menu")) {
            val mc = MinecraftClient.getInstance()
            mc.setScreen(GameMenuScreen(true))
        })
        component.focused = component.stageTree.activeStage
    }

    override fun shouldPause(): Boolean {
        return false
    }

    ///
    /// Allowing player to move while debugging
    ///
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        actionList.forEach {
            if (it.matchesKey(keyCode, scanCode)) {
                it.isPressed = true
            }
        }
        if (component.onKeyPress(keyCode, scanCode, modifiers))
            return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        actionList.forEach {
            if (it.matchesKey(keyCode, scanCode)) {
                it.isPressed = false
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun removed() {
        super.removed()
        component.focused = null
    }
}
