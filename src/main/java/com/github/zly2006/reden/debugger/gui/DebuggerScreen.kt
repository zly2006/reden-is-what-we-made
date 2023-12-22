package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.network.Continue
import com.github.zly2006.reden.network.StepInto
import com.github.zly2006.reden.network.StepOver
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.GameMenuScreen
import net.minecraft.text.Text

class DebuggerScreen(private val tree: TickStageTree, private val breakpoint: BreakPoint?): BaseOwoScreen<FlowLayout>() {
    val actionList by lazy {
        client!!.options.run {
            listOf(forwardKey, leftKey, backKey, rightKey, jumpKey, sneakKey)
        }
    }
    val stepOverButton = Components.button(Text.literal("Open Game Menu")) {
        val mc = MinecraftClient.getInstance()
        mc.setScreen(GameMenuScreen(true))
    }!!
    val component = object : DebuggerComponent(tree) {
        override var focused: TickStage?
            get() = super.focused
            set(value) {
                super.focused = value
                stepOverButton.active(value != null)
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
            child(
                Components.texture(
                    Reden.identifier("reden-icon.png"), 0, 0, 16, 16,
                    160, 160
                ).blend(true))
            child(Components.button(Text.literal("Step Into")) {
                ClientPlayNetworking.send(StepInto())
            })
            child(Components.button(Text.literal("Step Over")) {
                ClientPlayNetworking.send(StepOver(component.focused!!.id))
            })
        })
        rootComponent.child(stepOverButton)
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
