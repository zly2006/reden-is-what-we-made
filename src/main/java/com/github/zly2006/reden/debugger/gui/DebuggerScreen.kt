package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.gui.componments.TextureComponent
import com.github.zly2006.reden.network.Continue
import com.github.zly2006.reden.network.StepInto
import com.github.zly2006.reden.network.StepOver
import com.github.zly2006.reden.report.onFunctionUsed
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.GameMenuScreen
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

class DebuggerScreen(private val tree: TickStageTree, private val breakpoint: BreakPoint?): BaseOwoScreen<FlowLayout>() {
    val actionList by lazy {
        client!!.options.run {
            listOf(forwardKey, leftKey, backKey, rightKey, jumpKey, sneakKey)
        }
    }
    val stepOverButton = Components.button(Text.literal("Open Game Menu")) {
        onFunctionUsed("buttonOpenGameMenu_debuggerScreen")
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
        onFunctionUsed("init_debuggerScreen")
        rootComponent.child(component)
        rootComponent.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
            child(
                TextureComponent(
                    Reden.identifier("reden-icon.png"), 0, 0, 16, 16,
                    160, 160
                ).apply {
                    blend(true)
                    tooltip(Text.literal("Continue"))
                    mouseEnter().subscribe {
                        this.uv(0, 16)
                    }
                    mouseLeave().subscribe {
                        this.uv(0, 0)
                    }
                    mouseDown().subscribe { _, _, b ->
                        if (b == 0) {
                            client!!.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
                            ClientPlayNetworking.send(Continue())
                            true
                        } else false
                    }
                }
            )
            child(
                TextureComponent(
                    Reden.identifier("reden-icon.png"), 48, 0, 16, 16,
                    160, 160
                ).apply {
                    blend(true)
                    tooltip(Text.literal("Step Into"))
                    mouseDown().subscribe { _, _, b ->
                        if (b == 0) {
                            client!!.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
                            ClientPlayNetworking.send(StepInto())
                            true
                        } else false
                    }
                }
            )
            child(
                TextureComponent(
                    Reden.identifier("reden-icon.png"), 64, 0, 16, 16,
                    160, 160
                ).apply {
                    blend(true)
                    tooltip(Text.literal("Step Over"))
                    mouseDown().subscribe { _, _, b ->
                        if (b == 0) {
                            client!!.soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f))
                            ClientPlayNetworking.send(StepOver(component.focused!!.id))
                            true
                        } else false
                    }
                }
            )
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
        var flag = false
        actionList.forEach {
            if (it.matchesKey(keyCode, scanCode)) {
                it.isPressed = true
                flag = true
            }
        }
        if (flag) return true
        if (component.onKeyPress(keyCode, scanCode, modifiers))
            return true
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        var flag = false
        actionList.forEach {
            if (it.matchesKey(keyCode, scanCode)) {
                it.isPressed = false
                flag = true
            }
        }
        if (flag) return true
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun removed() {
        super.removed()
        component.focused = null
    }
}
