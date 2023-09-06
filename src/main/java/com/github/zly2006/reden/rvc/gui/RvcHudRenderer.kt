package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.utils.handToolItem
import fi.dy.masa.malilib.interfaces.IRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext

object RvcHudRenderer: IRenderer {
    override fun onRenderGameOverlayPost(drawContext: DrawContext) {
        val mc = MinecraftClient.getInstance()
        if (mc.currentScreen == null && mc.player?.handToolItem == true) {
            // Gui Hud
        }
    }
}
