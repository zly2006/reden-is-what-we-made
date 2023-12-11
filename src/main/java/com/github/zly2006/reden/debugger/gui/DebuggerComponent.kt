package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.tree.StageTree
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.network.Continue
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
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.GameMenuScreen
import net.minecraft.text.Text

class DebuggerComponent(
    val stageTree: TickStageTree
): FlowLayout(Sizing.content(), Sizing.fill(70), Algorithm.VERTICAL) {
    var focused: TickStage? = null
        set(value) {
            field?.unfocused(MinecraftClient.getInstance())
            field = value
            value?.focused(MinecraftClient.getInstance())
            children.clear()
            child(stageTreeLayout())
        }

    init {
        child(Containers.verticalScroll(Sizing.fill(100), Sizing.fill(60), stageTreeLayout()))
        // 30% height for infobox
    }

    class StageNodeComponent(
        val stage: TickStage,
        val lrWidth: Int = 20,
    ) : FlowLayout(Sizing.fill(100), Sizing.content(), Algorithm.HORIZONTAL) {
        init {
            child(Components.label(stage.displayName ?: Text.literal("null")).apply {
                this.tooltip(stage.description)
            })
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            context.fill(
                x,
                y,
                x + determineHorizontalContentSize(Sizing.content()),
                y + determineVerticalContentSize(Sizing.fill(100)),
                0x80_00_00_00.toInt()
            )
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }

    fun stageTreeLayout(): GridLayout {
        val layout = Containers.grid(Sizing.fill(100), Sizing.content(), stageTree.activeStages.size + 1, 3)
        layout.child(Components.label(Text.literal("Stage Tree")), 0, 1)
        stageTree.activeStages.mapIndexed { index, stage ->
            layout.child(StageNodeComponent(stage), index + 1, 0)
        }
        return layout
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

private fun StageTree.depth(): Int {
    var depth = 0
    var node = lastReturned
    while (node != null) {
        depth++
        node = node.parent
    }
    return depth
}
