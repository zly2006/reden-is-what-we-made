package com.github.zly2006.reden.rvc.gui

import com.github.zly2006.reden.malilib.HUD_POSITION
import com.github.zly2006.reden.utils.holdingToolItem
import fi.dy.masa.malilib.config.HudAlignment
import fi.dy.masa.malilib.interfaces.IRenderer
import fi.dy.masa.malilib.render.RenderUtils
import fi.dy.masa.malilib.util.GuiUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.OrderedText
import net.minecraft.text.Text

object RvcHudRenderer: IRenderer {
    val supplierMap = mutableMapOf<String, () -> List<Text>>()
    val conditionMap = mutableMapOf<String, () -> Boolean>()
    val lines = mutableMapOf<String, List<Text>>()

    fun updateLines() {
        lines.clear()
        supplierMap.forEach { (name, supplier) ->
            lines[name] = supplier()
        }
    }

    override fun onRenderGameOverlayPost(drawContext: DrawContext) {
        val mc = MinecraftClient.getInstance()
        if ((mc.player)?.holdingToolItem != true) return
        updateLines()
        val allLines = mutableListOf<OrderedText>()
        lines.forEach { (name, lines) ->
            if (conditionMap[name]?.invoke() != false) {
                allLines.addAll(lines.map { it.asOrderedText() })
            }
        }

        val scale = 1.0 // todo : malilib config  // fixme
        var xOff = 0    // todo : malilib config
        var yOff = 20   // todo : malilib config
        val alignment = HUD_POSITION.optionListValue as HudAlignment
        val textColor = 0xffffff
        val useShadow = true
        val bgColor = 0x80000000.toInt()

        val fontRenderer = mc.textRenderer
        val scaledWidth = GuiUtils.getScaledWindowWidth()
        val lineHeight = fontRenderer.fontHeight + 2
        val contentHeight = lines.values.flatten().size * lineHeight - 2
        val bgMargin = 2

        var posX: Double = (xOff + bgMargin).toDouble()
        var posY: Double = (yOff + bgMargin).toDouble()

        posY = RenderUtils.getHudPosY(posY.toInt(), yOff, contentHeight, scale, alignment).toDouble()
        posY += RenderUtils.getHudOffsetForPotions(alignment, scale, mc.player).toDouble()

        for (line in lines.values.flatten()) {
            val width = fontRenderer.getWidth(line)
            when (alignment) {
                HudAlignment.TOP_RIGHT, HudAlignment.BOTTOM_RIGHT -> posX = scaledWidth / scale - width - xOff - bgMargin
                HudAlignment.CENTER -> posX = scaledWidth / scale / 2 - width / 2 - xOff
                else -> {}
            }
            val x = posX.toInt()
            val y = posY.toInt()
            posY += lineHeight.toDouble()
            RenderUtils.drawRect(x - bgMargin, y - bgMargin, width + bgMargin, bgMargin + fontRenderer.fontHeight, bgColor)
            drawContext.drawText(fontRenderer, line, x, y, textColor, useShadow)
        }

        return
    }
}
