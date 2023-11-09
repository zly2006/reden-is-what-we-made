package com.github.zly2006.reden.gui

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text

class CreditScreen(val parent: Screen? = null): BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent.child(Components.label(Text.literal("Reden Credits"))
            .margins(Insets.vertical(15)))
        val content = Containers.verticalFlow(Sizing.fill(100), Sizing.content())
        rootComponent.child(Containers.verticalScroll(Sizing.fill(70), Sizing.fill(80), content))
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
        rootComponent.verticalAlignment(VerticalAlignment.CENTER)

        content.child(Components.label(
            Text.literal("Reden is an open source project under LGPL-3.0 license.").styled {
                it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to view Source and License.")))
                    .withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/zly2006/reden-is-what-we-made"))
            }
        ))
        content.child(center(Components.label(Text.literal("Open Source Project used by Reden"))
            .margins(Insets.vertical(15))))

        val openSourceGrid = Containers.grid(Sizing.fill(100), Sizing.content(), 20, 2)
        content.child(openSourceGrid)
        openSourceGrid.child(Components.label(Text.literal("Malilib")), 0, 0)
        openSourceGrid.child(Components.label(Text.literal("carpet")), 1, 0)
        openSourceGrid.child(Components.label(Text.literal("fabric-api")), 2, 0)
        openSourceGrid.child(Components.label(Text.literal("kotlin")), 3, 0)
        openSourceGrid.child(Components.label(Text.literal("okio")), 4, 0)
        openSourceGrid.child(Components.label(Text.literal("okhttp")), 5, 0)
        openSourceGrid.child(Components.label(Text.literal("jgit")), 6, 0)
        openSourceGrid.child(Components.label(Text.literal("gson")), 7, 0)
    }

    private fun center(component: Component): Component {
        return Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).configure<FlowLayout> {
            it.child(component)
            it.horizontalAlignment(HorizontalAlignment.CENTER)
        }
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackgroundTexture(context)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        client!!.setScreen(parent)
    }
}
