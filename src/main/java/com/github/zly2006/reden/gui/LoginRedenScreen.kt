package com.github.zly2006.reden.gui

import com.github.zly2006.reden.report.updateOnlineInfo
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.text.Text

class LoginRedenScreen(
    val reason: Text? = null
) : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
        rootComponent
            .child(Components.label(Text.literal("Login to Reden")))
            .child(Components.label(reason ?: Text.literal("Your operation requires login to Reden")))
            .child(Components.label(Text.literal("Please first bind your Microsoft account to Reden and verify your Minecraft ownership, then click this button to login")))
            .child(Components.button(Text.literal("Login")) {
                updateOnlineInfo(client!!)
            })
    }
}
