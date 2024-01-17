package com.github.zly2006.reden.rvc.gui.git

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

class RvcCommitScreen(
    horizontalSizing: Sizing, verticalSizing: Sizing
): FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {
    val client = MinecraftClient.getInstance()!!
    val refreshButton = Components.button(Text.empty()) {}
    val rollbackButton = Components.button(Text.empty()) {}

    val commitMessage = Components.textArea(Sizing.fill(), Sizing.content())

    val commitButton = Components.button(Text.empty()) {}
    val commitAndPushButton = Components.button(Text.empty()) {}

    init {
        TODO("Still designing")
        child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
            child(refreshButton)
            child(rollbackButton)
        })
        child(Containers.verticalScroll(Sizing.fill(), Sizing.fill(), Containers.verticalFlow(Sizing.fill(), Sizing.content()).apply {
            child(commitMessage)
            child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                child(commitButton)
                child(commitAndPushButton)
            })
        }))
    }

    class Screen: BaseOwoScreen<RvcCommitScreen>() {
        override fun createAdapter() = OwoUIAdapter.create(this, ::RvcCommitScreen)!!

        override fun build(rootComponent: RvcCommitScreen) {
            rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
        }
    }
}
