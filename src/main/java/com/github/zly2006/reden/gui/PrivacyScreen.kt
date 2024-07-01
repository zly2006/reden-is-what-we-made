package com.github.zly2006.reden.gui

import com.github.zly2006.reden.malilib.HiddenOption.data_BASIC
import com.github.zly2006.reden.malilib.HiddenOption.data_IDENTIFICATION
import com.github.zly2006.reden.malilib.HiddenOption.data_USAGE
import com.github.zly2006.reden.malilib.HiddenOption.iPRIVACY_SETTING_SHOWN
import com.github.zly2006.reden.report.onFunctionUsed
import com.github.zly2006.reden.report.updateOnlineInfo
import com.github.zly2006.reden.saveMalilibOptions
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer.Scrollbar
import io.wispforest.owo.ui.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class PrivacyScreen(val parent: Screen? = null): BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        iPRIVACY_SETTING_SHOWN.booleanValue = true
        saveMalilibOptions()
        onFunctionUsed("init_privacyScreen")
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
        rootComponent.verticalAlignment(VerticalAlignment.CENTER)
        rootComponent.child(Components.label(Text.literal("Reden Privacy Settings"))
                .shadow(true)
                .margins(Insets.of(10)))
        val content = Containers.verticalFlow(Sizing.fill(100), Sizing.content())
        rootComponent.child(Containers.verticalScroll(Sizing.fill(70), Sizing.fill(80), content).apply {
            scrollbar(Scrollbar.vanillaFlat())
        })

        content.child(Components.label(Text.translatable("reden.widget.privacy.desc"))
            .horizontalTextAlignment(HorizontalAlignment.LEFT)
            .margins(Insets.vertical(3))
            .horizontalSizing(Sizing.fill(100)))

        content.child(Components.button(Text.literal("Continue")) {
            onFunctionUsed("continue_privacyScreen")
            saveMalilibOptions()
            this.close()
        })
        content.child(Components.smallCheckbox(Text.literal("Basic System Data")).checked(data_BASIC.booleanValue).apply {
            this.onChanged().subscribe {
                data_BASIC.booleanValue = it
            }
        })
        content.child(Components.smallCheckbox(Text.literal("Usage Data")).checked(data_USAGE.booleanValue).apply {
            this.onChanged().subscribe {
                data_USAGE.booleanValue = it
            }
        })
        content.child(Components.smallCheckbox(Text.literal("Identification Data")).checked(data_IDENTIFICATION.booleanValue).apply {
            this.onChanged().subscribe {
                data_IDENTIFICATION.booleanValue = it
            }
        })
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        renderInGameBackground(context)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun close() {
        client!!.setScreen(parent)
        saveMalilibOptions()
        if (data_IDENTIFICATION.booleanValue) {
            GlobalScope.launch(Dispatchers.IO) {
                updateOnlineInfo(client!!)
            }.start()
        }
    }
}
