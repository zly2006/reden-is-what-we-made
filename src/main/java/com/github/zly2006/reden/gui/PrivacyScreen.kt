package com.github.zly2006.reden.gui

import com.github.zly2006.reden.RedenClient
import com.github.zly2006.reden.malilib.data_BASIC
import com.github.zly2006.reden.malilib.data_IDENTIFICATION
import com.github.zly2006.reden.malilib.data_USAGE
import com.github.zly2006.reden.malilib.iPRIVACY_SETTING_SHOWN
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class PrivacyScreen(val parent: Screen? = null): BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        iPRIVACY_SETTING_SHOWN.booleanValue = true
        RedenClient.saveMalilibOptions()
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
        rootComponent.verticalAlignment(VerticalAlignment.CENTER)
        rootComponent.child(Components.label(Text.literal("Reden Privacy Settings"))
                .shadow(true)
                .margins(Insets.of(10)))
        val content = Containers.verticalFlow(Sizing.fill(100), Sizing.content())
        rootComponent.child(Containers.verticalScroll(Sizing.fill(70), Sizing.fill(80), content))

        content.child(Components.label(Text.translatable("reden.widget.privacy.desc"))
            .horizontalTextAlignment(HorizontalAlignment.LEFT)
            .margins(Insets.vertical(3))
            .horizontalSizing(Sizing.fill(100)))

        content.child(Components.button(Text.literal("Continue")) {
            RedenClient.saveMalilibOptions()
            this.close()
        })
        content.child(Components.smallCheckbox(Text.literal("Basic System Data")).checked(true).apply {
            this.onChanged().subscribe {
                data_BASIC.booleanValue = it
            }
        })
        content.child(Components.smallCheckbox(Text.literal("Usage Data")).checked(true).apply {
            this.onChanged().subscribe {
                data_USAGE.booleanValue = it
            }
        })
        content.child(Components.smallCheckbox(Text.literal("Identification Data")).checked(true).apply {
            this.onChanged().subscribe {
                data_IDENTIFICATION.booleanValue = it
            }
        })
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackgroundTexture(context)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        client!!.setScreen(parent)
    }
}
