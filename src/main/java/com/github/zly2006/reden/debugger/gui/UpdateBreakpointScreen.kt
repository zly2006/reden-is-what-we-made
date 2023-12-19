package com.github.zly2006.reden.debugger.gui

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.GridLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.text.Text

class UpdateBreakpointScreen(
    val breakpoint: BreakPoint
): BaseOwoScreen<ScrollContainer<FlowLayout>>() {
    private var previousName: String = breakpoint.name
    val nameInput: TextBoxComponent = Components.textBox(Sizing.fill(70))!!.apply {
        tooltip(Text.literal("Rename this"))
        onChanged().subscribe {
            if (it != previousName) renameButton.active(true)
            else renameButton.active(false)
        }
    }
    val renameButton = Components.button(Text.literal("Rename")) {
        if (nameInput.text != previousName) {
            breakpoint.name = nameInput.text
            previousName = nameInput.text
        }
    }!!
    val infoMetric = object: GridLayout(Sizing.fill(100), Sizing.content(), 3, 2){
        init {
            child(Components.label(Text.literal("Type: ")), 0, 0)
            child(Components.label(breakpoint.type.description), 0, 1)

            child(Components.label(Text.literal("Position: ")), 1, 0)
            child(Components.label(Text.literal(breakpoint.pos?.toShortString())), 1, 1)

            child(Components.label(Text.literal("World: ")), 2, 0)
            child(Components.label(Text.literal(breakpoint.world?.toString())), 2, 1)
        }

        override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
            context.fill(
                x,
                y,
                x + determineHorizontalContentSize(Sizing.content()),
                y + determineVerticalContentSize(Sizing.fill(100)),
                0x80000000.toInt()
            )
            super.draw(context, mouseX, mouseY, partialTicks, delta)
        }
    }
    override fun createAdapter() = OwoUIAdapter.create(this) { horizontal, vertical ->
        Containers.verticalScroll(horizontal, vertical, Containers.verticalFlow(horizontal, vertical))
    }!!

    override fun build(p0: ScrollContainer<FlowLayout>) {
        val root = p0.child()
        root.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
            child(nameInput)
            child(renameButton)
        })
        root.child(infoMetric)
    }

    class BreakpointBehaviorComponent {
    }
}
