package com.github.zly2006.reden.sponsor

import com.github.zly2006.reden.report.onFunctionUsed
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
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

class SponsorScreen(val parent: Screen? = null, private val loadIfNull: Boolean = true): BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        onFunctionUsed("init_viewSponsors")
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
        rootComponent.child(Components.label(Text.literal("Reden's Sponsors")).shadow(true).margins(Insets.top(10)))
        rootComponent.child(Components.label(Text.literal("We are very grateful to the following people for their support.").formatted(Formatting.GRAY)).margins(Insets.bottom(10)))
        if (sponsors == null) {
            if (loadIfNull) {
                rootComponent.child(Components.label(Text.literal("Loading sponsors...").formatted(Formatting.GRAY)))
                updateSponsors()
            }
            else {
                rootComponent.child(Components.label(Text.literal("Sorry, failed to load sponsors.").formatted(Formatting.RED)))
            }
        }
        else {
            val list = Containers.verticalFlow(Sizing.fill(100), Sizing.content())
            sponsors!!.forEach {
                list.child(labelComponent(Text.literal(it.name)).shadow(true).margins(Insets.of(10, 5, 0, 0)))
                val display = (if (it.detail.isNullOrEmpty()) "¥${it.amount}" else it.detail) +
                        (if (it.message.isEmpty()) "" else "\n${it.message}")
                list.child(labelComponent(Text.literal(display).formatted(Formatting.GRAY)))
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

private fun labelComponent(text: Text): LabelComponent {
    return Components.label(text).configure {
        it.horizontalSizing(Sizing.fill(100))
    }
}
