package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.gui.QuickMenuWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.max

class DebuggerTimelineScreen : Screen(Text.literal("reden")) {
    var horizontalScale = 50.0
    var horizontalOffset = 0.0
    var maxTicks = 8
    var labelWidth = 50
    val timelineHeight = 30
    var quickMenu: QuickMenuWidget? = null

    class ColumnReference(
        val width: Int,
        val serverTicks: Int,
        val thisIndex: Int,
        val parent: ColumnReference? = null,
        var displayed: Boolean = true
    ) : Comparable<ColumnReference> {
        override fun compareTo(other: ColumnReference): Int {
            if (!displayed && !other.displayed) {
                return 0
            }
            if (!displayed) {
                return 1
            }
            if (!other.displayed) {
                return -1
            }
            if (serverTicks > other.serverTicks) {
                return 1
            }
            if (serverTicks < other.serverTicks) {
                return -1
            }
            if (thisIndex > other.serverTicks) {
                return 1
            }
            if (thisIndex < other.serverTicks) {
                return -1
            }
            return 0
        }
    }

    inner class Row(
        val label: Text,
        var height: Int = 10
    ) {
        var y = timelineHeight
        val entries = mutableListOf<Entry>()
        fun render(drawContext: DrawContext, mouseX: Int, mouseY: Int) {
            if (mouseY >= y && mouseY < y + height) {
                drawContext.fill(0, y, width, y + height, 0x70666666)
            }

            val tr = client!!.textRenderer
            drawContext.drawTextWrapped(tr, label, 3, y + (height - 8) / 2, labelWidth, 0xffffff)
            drawContext.drawHorizontalLine(0, width, y, 0x808080)
            drawContext.drawHorizontalLine(0, width, y + height, 0x808080)

            drawContext.enableScissor(labelWidth, y, width, y + height)
            for (entry in entries) {
                drawContext.fill(entry.x, y, entry.x + entry.width, y + height, entry.color)
                if (entry.hovered(mouseX, mouseY)) {
                    drawContext.fill(
                        entry.x + 1,
                        y + 1,
                        entry.x + entry.width - 1,
                        y + height - 1,
                        ligthenColor(entry.color)
                    )
                }
                val textX = max(labelWidth, entry.x) + 3
                drawContext.drawTextWrapped(
                    tr, entry.text,
                    textX, y + (height - 8) / 2,
                    entry.x + entry.width - textX, 0xffffff
                )
            }
            drawContext.disableScissor()
        }

        fun postRender(drawContext: DrawContext, mouseX: Int, mouseY: Int) {
        }

        open inner class Entry(
            // negative
            val startTick: Double,
            // negative
            val endTick: Double,
            val color: Int,
            val text: Text
        ) {
            val x: Int get() = ((maxTicks + startTick) * horizontalScale - horizontalOffset).toInt() + labelWidth
            val width: Int get() = ((endTick - startTick) * horizontalScale).toInt()
            open fun rightClick(mouseX: Int, mouseY: Int) {
            }

            fun hovered(mouseX: Int, mouseY: Int): Boolean {
                return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height
            }
        }
    }

    private fun ligthenColor(color: Int): Int {
        val alpha = color ushr 24
        val red = color ushr 16 and 0xff
        val green = color ushr 8 and 0xff
        val blue = color and 0xff
        return (alpha shl 24) or
                ((red + (255 - red) / 3) shl 16) or
                ((green + (255 - green) / 3) shl 8) or
                (blue + (255 - blue) / 3)
    }

    val rows = listOf(
        Row(
            Text.literal("Test1")
        ).apply {
            entries.add(Entry(-4.0, -2.0, 0xff4f644f.toInt(), Text.literal("a")))
        },
        Row(
            Text.literal("Test2")
        ).apply {
            entries.add(object : Row.Entry(-3.0, -1.0, 0xff7c7cff.toInt(), Text.literal("Right click")) {
                override fun rightClick(mouseX: Int, mouseY: Int) {
                    quickMenu = QuickMenuWidget(
                        this@DebuggerTimelineScreen, mouseX,
                        mouseY
                    ).apply {
                        addEntry(Text.literal("Example")) { e, _ ->
                            e.name = Text.literal("Done")
                            e.action = QuickMenuWidget.EMPTY_ACTION
                        }
                    }
                    super.rightClick(mouseX, mouseY)
                }
            })
        }
    )

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (horizontalOffset < 0) {
            horizontalOffset = 0.0
        }
        if (horizontalOffset > max(0.0, maxTicks * horizontalScale - this.width + labelWidth + 30)) {
            horizontalOffset = maxTicks * horizontalScale - this.width + labelWidth + 30
        }

        val tr = client!!.textRenderer
        context.drawVerticalLine(labelWidth, 0, height, 0x808080)
        // render timeline

        renderTimeline(context)

        var y = timelineHeight
        for (row in rows) {
            row.y = y
            row.render(context, mouseX, mouseY)
            y += row.height
        }
        y = timelineHeight
        for (row in rows) {
            row.y = y
            row.postRender(context, mouseX, mouseY)
            y += row.height
        }

        if (quickMenu != null) {
            quickMenu!!.render(context, mouseX, mouseY, delta)
        }
    }

    override fun remove(child: Element?) {
        if (child == quickMenu) {
            quickMenu = null
        }
        super.remove(child)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        val d = (mouseX - labelWidth + horizontalOffset) / horizontalScale
        horizontalScale += verticalAmount * 40 / horizontalScale
        horizontalOffset = -mouseX + labelWidth + d * horizontalScale

        horizontalOffset -= horizontalAmount * 4
        if (horizontalScale < (width - labelWidth - 30).toDouble() / maxTicks) {
            horizontalScale = (width - labelWidth - 30).toDouble() / maxTicks
        }
        if (horizontalOffset < 0) {
            horizontalOffset = 0.0
        }
        quickMenu = null
        return true
    }

    private fun renderTimeline(context: DrawContext) {
        val tr = client!!.textRenderer
        context.fill(labelWidth, 0, width, timelineHeight, 0xff404040.toInt())
        context.drawHorizontalLine(labelWidth, width, timelineHeight, 0x808080)
        context.drawHorizontalLine(labelWidth, width, timelineHeight / 2, 0x808080)
        var lastRenderedEndAt = labelWidth
        for (i in 0..maxTicks) {
            val tick = i - maxTicks // negative
            val timeLabel = Text.literal("${tick}gt")
            val timeLabelWidth = tr.getWidth(timeLabel)
            val x = labelWidth + (i * horizontalScale - horizontalOffset).toInt()
            if (x >= lastRenderedEndAt && x + timeLabelWidth + 2 <= width) {
                context.drawVerticalLine(x, 0, timelineHeight, 0xffffffff.toInt())
                context.drawText(tr, timeLabel, x + 3, 0, 0xffffff, false)
                lastRenderedEndAt = x + timeLabelWidth + 4
            }
        }

        context.drawHorizontalLine(0, width, timelineHeight, 0xff808080.toInt())
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 1) {
            for (row in rows) {
                if (mouseY >= row.y && mouseY < row.y + row.height) {
                    for (entry in row.entries) {
                        if (entry.hovered(mouseX.toInt(), mouseY.toInt())) {
                            entry.rightClick(mouseX.toInt(), mouseY.toInt())
                            return true
                        }
                    }
                }
            }
        }
        quickMenu?.mouseClicked(mouseX, mouseY, button)
        return super.mouseClicked(mouseX, mouseY, button)
    }
}
