package com.github.zly2006.reden.rvc.gui

import fi.dy.masa.malilib.interfaces.IRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

object RvcHudRenderer: IRenderer {
    val supplierMap = mutableMapOf<String, () -> List<Text>>()
    val conditionMap = mutableMapOf<String, () -> Boolean>()
    val dirty = mutableMapOf<String, Boolean>()

    fun markDirty(name: String) {
        dirty[name] = true
    }

    override fun onRenderGameOverlayPost(drawContext: DrawContext) {
        super.onRenderGameOverlayPost(drawContext)
    }
}
