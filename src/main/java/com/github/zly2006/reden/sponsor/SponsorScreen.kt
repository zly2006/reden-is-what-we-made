package com.github.zly2006.reden.sponsor

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class SponsorScreen(val parent: Screen? = null): BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
        rootComponent.child(Components.label(Text.literal("Reden's Sponsors")))
        rootComponent.child(Components.label(Text.literal("We are very grateful to the following people for their support.").formatted(Formatting.GRAY)))
        if (sponsors == null) {
            rootComponent.child(Components.label(Text.literal("Loading sponsors...").formatted(Formatting.GRAY)))
            updateSponsors()
        } else if (sponsors!!.isEmpty()) {
            rootComponent.child(Components.label(Text.literal("Sorry, failed to load sponsors.").formatted(Formatting.RED)))
        }
        else {
            val list = Containers.verticalFlow(Sizing.fill(100), Sizing.content())
            sponsors!!.forEach {
                list.child(Components.label(Text.literal(it.name)).shadow(true).margins(Insets.top(10)))
                list.child(Components.label(Text.literal((it.detail ?: "¥${it.amount}") + it.message).formatted(Formatting.GRAY)))
            }
            rootComponent.child(Containers.verticalScroll(Sizing.fill(70), Sizing.fill(80), list))
        }
    }

    override fun close() {
        client!!.setScreen(parent)
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackgroundTexture(context)
        super.render(context, mouseX, mouseY, delta)
    }
}
