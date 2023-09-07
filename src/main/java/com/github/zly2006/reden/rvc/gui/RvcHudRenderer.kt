package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.utils.handToolItem
import fi.dy.masa.malilib.interfaces.IRenderer
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

object RvcHudRenderer: IRenderer {
    val root = Containers.verticalFlow(Sizing.content(), Sizing.content())
    override fun onRenderGameOverlayPost(drawContext: DrawContext) {
        val mc = MinecraftClient.getInstance()
        if (mc.currentScreen == null && mc.player?.handToolItem == true) {
            // Gui Hud
            // fixme: not working
            val component: Component = Components.label(Text.of("RVC"))
            component.draw(
                OwoUIDrawContext.of(drawContext),
                0, 0, 0f, 0f
            )
        }
    }
}
